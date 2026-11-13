/* SPDX-License-Identifier: GPL-2.0 */
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/kallsyms.h>
#include <linux/string.h>
#include <linux/version.h>
#include <linux/vmalloc.h>
#include <linux/log2.h>
#include <linux/overflow.h>
#include <linux/spinlock.h>
#include <linux/limits.h>

#include "symbol_resolver.h"
#include "susfs_log.h"

#include "kallrecon/lib/core.h"
#include "kallrecon/lib/hint.h"
#include "kallrecon/lib/slide.h"
#include "kallrecon/lib/symbol.h"
#include "kallrecon/lib/variant.h"

#define KR_SCAN_BACK	0x400000UL
#define KR_SCAN_FWD	0x200000UL
#define KR_TIMEOUT_MS	10000

#define KR_NAME_MAX	256
#define KR_ALL_MAX	8

static bool kr_ready;

static bool kr_layout_ready(void)
{
	return klnum_val && kloffs_addr && klnames_addr;
}

static bool ks_idx_first(const char *name, unsigned long *addr);

/* Exact-name lookup.  While the one-shot index exists (module init) it answers from there: the
 * kernel's own kallsyms_lookup_name() is a linear scan too, and init makes hundreds of such calls.
 * A name the index does not have (module symbols, ...) still goes to the kernel. */
static __nocfi noinline unsigned long kr_klp(const char *name)
{
	unsigned long addr;

	if (ks_idx_first(name, &addr))
		return addr;
	return kallrecon_klp(name);
}

static const char *(*kr_lookup_fn)(unsigned long addr, unsigned long *symbolsize,
				   unsigned long *offset, char **modname, char *namebuf);

static __nocfi noinline const char *kr_lookup(unsigned long addr, char **modname, char *buf)
{
	return kr_lookup_fn(addr, NULL, NULL, modname, buf);
}

unsigned long find_kernel_symbol_exact_any(const char *symbol_name)
{
	if (!symbol_name || !symbol_name[0] || !kr_ready || !kallrecon_klp)
		return 0;

	return kr_klp(symbol_name);
}

unsigned long ksu_kallsyms_lookup_name(const char *name)
{
	return find_kernel_symbol_exact_any(name);
}

unsigned long find_kernel_symbol_exact(const char *symbol_name)
{
	unsigned long addr;
	char buf[KSYM_SYMBOL_LEN];
	char *mod = NULL;

	addr = find_kernel_symbol_exact_any(symbol_name);
	if (!addr || !kr_lookup_fn)
		return 0;

	if (!kr_lookup(addr, &mod, buf))
		return 0;

	if (mod) {
		pr_warn("ignore symbol %s of module %s\n", symbol_name, mod);
		return 0;
	}

	return addr;
}

void *ksu_resolve_symbol_for_functable_hook(const char *symbol_name)
{
	unsigned long addr;

	if (!kr_ready || !symbol_name || !symbol_name[0])
		return NULL;

#if LINUX_VERSION_CODE >= KERNEL_VERSION(6, 1, 0)
	addr = find_kernel_symbol_exact(symbol_name);
	if (!addr)
		addr = kallrecon_find_variant(symbol_name, 0);
#else
	{
		char cfi_name[KSYM_NAME_LEN];

		snprintf(cfi_name, sizeof(cfi_name), "%s.cfi_jt", symbol_name);
		addr = find_kernel_symbol_exact(cfi_name);
		if (!addr)
			addr = kallrecon_find_variant(symbol_name, 1);
		if (!addr)
			addr = find_kernel_symbol_exact(symbol_name);
	}
#endif

	return addr ? (void *)addr : NULL;
}

struct kr_collect {
	unsigned long *addrs;
	int max;
	int n;
};

static void kr_collect_add(struct kr_collect *c, unsigned long addr)
{
	int i;

	if (!addr)
		return;
	for (i = 0; i < c->n; i++)
		if (c->addrs[i] == addr)
			return;
	if (c->n < c->max)
		c->addrs[c->n++] = addr;
}

/*
 * One-shot name index for ksu_find_symbol_all().
 *
 * The linear scan below decompresses every kallsyms name (~200k of them) on each call, and module
 * init makes on the order of a hundred such calls.  On slow cores that adds up to seconds, and
 * vendor hang detectors (seen on Unisoc: "susfs_guard_lkm.ko loads too long time, panic timeout =
 * 2000 ms") turn a slow init_module into a kernel panic.  So the first call walks the table once
 * and records, per name, where its entry lives; later calls hash the query, probe a few slots and
 * re-expand only the candidate entries.  The table is dropped when init finishes
 * (ksu_symbol_index_release); a later lookup simply rebuilds it.
 *
 * Results are identical to the scan: matches are returned in kallsyms order (ascending index).
 */
struct ks_idx_slot {
	u32 idx1;	/* symbol index + 1, 0 = empty slot */
	u32 off;	/* offset of the entry inside the names blob */
};

/* ks_idx_tab/ks_idx_mask are published and retired under ks_idx_lock (lookups may run in any
 * context); building and the `tried` flag are serialized by ks_linear_lock like the plain scan. */
static DEFINE_SPINLOCK(ks_idx_lock);
static struct ks_idx_slot *ks_idx_tab;
static unsigned int ks_idx_mask;
static bool ks_idx_tried;

#define KS_IDX_HITS	16

static u32 ks_name_hash(const char *s)
{
	u32 h = 2166136261u;

	while (*s) {
		h ^= (unsigned char)*s++;
		h *= 16777619u;
	}
	return h;
}

/* caller holds ks_linear_lock */
static void ks_idx_build(void)
{
	struct slide_win w;
	char nbuf[KR_NAME_MAX];
	struct ks_idx_slot *tab;
	unsigned int size, idx = 0;
	unsigned long flags;
	int retried = 0;

	ks_idx_tried = true;
	if (!klnum_val || klnum_val > (1U << 24))
		return;

	size = roundup_pow_of_two(klnum_val * 2);
	tab = vzalloc(array_size(size, sizeof(*tab)));
	if (!tab)
		return;

	if (safe_read(ti_buf, (void *)klindex_addr, sizeof(ti_buf)) ||
	    safe_read(tt_buf, (void *)kltable_addr, sizeof(tt_buf)) ||
	    slide_init(&w, klnames_addr, KS_WIN_SIZE, KS_WIN_MARGIN)) {
		vfree(tab);
		return;
	}

	while (idx < klnum_val) {
		const unsigned char *enc = slide_ptr(&w, slide_buf);
		unsigned int len = enc[0];
		unsigned int hdr = 1;

		if (len & 0x80) {
			len = (len & 0x7f) | (enc[1] << 7);
			hdr = 2;
		}

		if (len > KR_NAME_MAX || w.off + hdr + len > w.valid) {
			if (retried || slide_advance(&w, (w.valid - w.off) + 4))
				break;
			retried = 1;
			continue;
		}
		retried = 0;

		if (ks_expand_raw(enc, ti_buf, tt_buf, nbuf, sizeof(nbuf))) {
			unsigned int slot = ks_name_hash(nbuf) & (size - 1);

			while (tab[slot].idx1)
				slot = (slot + 1) & (size - 1);
			tab[slot].idx1 = idx + 1;
			tab[slot].off = (u32)(slide_addr(&w) - klnames_addr);
		}

		if (slide_advance(&w, hdr + len))
			break;
		idx++;
	}

	spin_lock_irqsave(&ks_idx_lock, flags);
	ks_idx_mask = size - 1;
	ks_idx_tab = tab;
	spin_unlock_irqrestore(&ks_idx_lock, flags);
}

/* fills c in kallsyms order; false = no index, caller falls back to the plain scan */
static bool ks_idx_lookup(const char *name, struct kr_collect *c)
{
	unsigned int hit[KS_IDX_HITS];
	char ebuf[KR_NAME_MAX];
	unsigned long flags;
	unsigned int slot;
	int nh = 0, i, j;

	spin_lock_irqsave(&ks_idx_lock, flags);
	if (!ks_idx_tab) {
		spin_unlock_irqrestore(&ks_idx_lock, flags);
		return false;
	}
	slot = ks_name_hash(name) & ks_idx_mask;
	while (ks_idx_tab[slot].idx1) {
		if (expand_sym(ks_idx_tab[slot].off, ebuf, sizeof(ebuf)) &&
		    !strcmp(ebuf, name) && nh < KS_IDX_HITS)
			hit[nh++] = ks_idx_tab[slot].idx1 - 1;
		slot = (slot + 1) & ks_idx_mask;
	}
	spin_unlock_irqrestore(&ks_idx_lock, flags);

	/* insertion sort: the scan reports matches in ascending symbol index */
	for (i = 1; i < nh; i++) {
		unsigned int v = hit[i];

		for (j = i - 1; j >= 0 && hit[j] > v; j--)
			hit[j + 1] = hit[j];
		hit[j + 1] = v;
	}
	for (i = 0; i < nh; i++)
		kr_collect_add(c, sym_addr((int)hit[i]));
	return true;
}

/* First match in kallsyms order, like kallsyms_lookup_name(); false = not answered here. */
static bool ks_idx_first(const char *name, unsigned long *addr)
{
	char ebuf[KR_NAME_MAX];
	unsigned int best = UINT_MAX;
	unsigned long flags;
	unsigned int slot;

	if (!name || !name[0])
		return false;

	spin_lock_irqsave(&ks_idx_lock, flags);
	if (!ks_idx_tab) {
		spin_unlock_irqrestore(&ks_idx_lock, flags);
		return false;
	}
	slot = ks_name_hash(name) & ks_idx_mask;
	while (ks_idx_tab[slot].idx1) {
		unsigned int idx = ks_idx_tab[slot].idx1 - 1;

		if (idx < best && expand_sym(ks_idx_tab[slot].off, ebuf, sizeof(ebuf)) &&
		    !strcmp(ebuf, name))
			best = idx;
		slot = (slot + 1) & ks_idx_mask;
	}
	spin_unlock_irqrestore(&ks_idx_lock, flags);

	if (best == UINT_MAX)
		return false;
	*addr = sym_addr((int)best);
	return *addr != 0;
}

/* Build the index now (module init, process context). */
static void ks_idx_prepare(void)
{
	mutex_lock(&ks_linear_lock);
	if (!ks_idx_tab && !ks_idx_tried)
		ks_idx_build();
	mutex_unlock(&ks_linear_lock);
}

void ksu_symbol_index_release(void)
{
	struct ks_idx_slot *tab;
	unsigned long flags;

	mutex_lock(&ks_linear_lock);
	spin_lock_irqsave(&ks_idx_lock, flags);
	tab = ks_idx_tab;
	ks_idx_tab = NULL;
	ks_idx_mask = 0;
	spin_unlock_irqrestore(&ks_idx_lock, flags);
	ks_idx_tried = false;
	mutex_unlock(&ks_linear_lock);

	vfree(tab);
}

int ksu_find_symbol_all(const char *name, unsigned long *addrs, int max)
{
	struct slide_win w;
	struct kr_collect c = { .addrs = addrs, .max = max };
	char nbuf[KR_NAME_MAX];
	unsigned int idx = 0;
	int retried = 0;

	if (!kr_ready || !name || !name[0] || !addrs || max <= 0)
		return 0;

	mutex_lock(&ks_linear_lock);
	if (!ks_idx_tab && !ks_idx_tried)
		ks_idx_build();
	if (ks_idx_lookup(name, &c))
		goto out;

	/* index unavailable (allocation failed, ...): fall back to the plain scan */
	if (safe_read(ti_buf, (void *)klindex_addr, sizeof(ti_buf)) ||
	    safe_read(tt_buf, (void *)kltable_addr, sizeof(tt_buf)) ||
	    slide_init(&w, klnames_addr, KS_WIN_SIZE, KS_WIN_MARGIN))
		goto out;

	while (idx < klnum_val) {
		const unsigned char *enc = slide_ptr(&w, slide_buf);
		unsigned int len = enc[0];
		unsigned int hdr = 1;

		if (len & 0x80) {
			len = (len & 0x7f) | (enc[1] << 7);
			hdr = 2;
		}

		if (len > KR_NAME_MAX || w.off + hdr + len > w.valid) {
			if (retried || slide_advance(&w, (w.valid - w.off) + 4))
				break;
			retried = 1;
			continue;
		}
		retried = 0;

		if (ks_expand_raw(enc, ti_buf, tt_buf, nbuf, sizeof(nbuf)) &&
		    !strcmp(nbuf, name))
			kr_collect_add(&c, sym_addr((int)idx));

		if (slide_advance(&w, hdr + len))
			break;
		idx++;
	}

out:
	mutex_unlock(&ks_linear_lock);
	return c.n;
}

int ksu_symbol_name_of(unsigned long addr, char *buf, char **module_out)
{
	char *mod = NULL;

	if (module_out)
		*module_out = NULL;
	if (!buf)
		return -EINVAL;
	buf[0] = '\0';
	if (!kr_ready)
		return -ENOSYS;
	if (!addr)
		return -EINVAL;

	if (kr_lookup_fn) {
		if (!kr_lookup(addr, &mod, buf))
			return -ENOENT;
		if (module_out)
			*module_out = mod;
		return (int)strlen(buf);
	}

	if (sym_name_at(addr, buf, KSYM_SYMBOL_LEN) < 0)
		return -ENOENT;

	return (int)strlen(buf);
}

int ksu_symbol_is_start(unsigned long addr, const char *name)
{
	char buf[KSYM_SYMBOL_LEN];
	size_t len, nlen;
	int idx;

	if (!kr_ready || !addr || !name || !name[0])
		return 0;

	len = strlen(name);
	if (len >= sizeof(buf))
		return 0;

	idx = sym_name_at(addr, buf, sizeof(buf));
	nlen = strlen(buf);
	if (idx >= 0 && sym_addr(idx) == addr && nlen >= len &&
	    !strncmp(buf, name, len) &&
	    (!buf[len] || buf[len] == '.' || buf[len] == '$') &&
	    !strstr(buf + len, ".cfi_jt"))
		return 1;

	return ksu_addr_is_named(addr, name);
}

int ksu_addr_is_named(unsigned long addr, const char *name)
{
	unsigned long addrs[KR_ALL_MAX];
	int n, i;

	if (!kr_ready || !addr || !name || !name[0])
		return 0;

	n = ksu_find_symbol_all(name, addrs, (int)ARRAY_SIZE(addrs));
	for (i = 0; i < n; i++)
		if (addrs[i] == addr)
			return 1;

	return 0;
}

int __init ksu_init_symbol_resolver(void)
{
	struct kallrecon_hint hint;

	memset(&hint, 0, sizeof(hint));
	hint.timeout_ms = KR_TIMEOUT_MS;
	kallrecon_supply(&hint);

	find_kallsyms_base();

	if (!kr_layout_ready() || !kallrecon_klp) {
		memset(&hint, 0, sizeof(hint));
		hint.scan_back = KR_SCAN_BACK * 2;
		hint.scan_fwd = KR_SCAN_FWD * 2;
		hint.timeout_ms = KR_TIMEOUT_MS;
		kallrecon_supply(&hint);
		kallrecon_retry();
	}

	if (!kr_layout_ready() || !kallrecon_klp) {
		pr_err("symbol_resolver: kallrecon discovery failed (%d, klp=%d), refusing to load\n",
		       kallrecon_fail_reason(), !!kallrecon_klp);
		return -ENODATA;
	}

	kr_ready = true;

	kr_lookup_fn = (void *)kr_klp("kallsyms_lookup");
	if (!kr_lookup_fn)
		pr_warn("symbol_resolver: kallsyms_lookup not resolved, module names unavailable\n");

	SUSFS_LOGI("symbol_resolver: kallrecon layout v%d base=0x%lx num=%u klp=%d\n",
		   (int)kl_layout, kernel_base, klnum_val, !!kallrecon_klp);

	/* From here on init does hundreds of lookups; see the index comment above. */
	ks_idx_prepare();

	return 0;
}
