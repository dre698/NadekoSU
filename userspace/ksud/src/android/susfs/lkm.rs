//! Load / unload the embedded SUSFS guard LKM (`susfs_guard_lkm.ko`).
//!
//! The module is built per KMI by CI and embedded in ksud as
//! `<kmi>_susfs_guard_lkm.ko`. It is only meant for kernels that do not ship
//! SUSFS in-tree; if SUSFS is already answering we never load it.

use std::ffi::CString;
use std::fs;

use anyhow::{Context, Result, ensure};
use log::{info, warn};

use crate::{
    android::susfs::api::prelude as api,
    boot_patch::get_current_kmi,
    defs::SUSFS_LKM_AUTOLOAD,
};

const MODULE_NAME: &std::ffi::CStr = c"susfs_guard_lkm";

fn asset_name(kmi: &str) -> String {
    format!("{kmi}_susfs_guard_lkm.ko")
}

fn validate_kmi(kmi: &str) -> Result<()> {
    ensure!(
        !kmi.is_empty()
            && kmi.len() <= 64
            && kmi
                .bytes()
                .all(|b| b.is_ascii_alphanumeric() || matches!(b, b'.' | b'_' | b'-')),
        "Invalid KMI '{kmi}'"
    );
    Ok(())
}

/// Version string if SUSFS (in-kernel or LKM) is already answering.
fn detected_version() -> Option<String> {
    api::version().ok().filter(|v| v.starts_with('v'))
}

fn read_marker() -> Option<Option<String>> {
    let content = fs::read_to_string(SUSFS_LKM_AUTOLOAD).ok()?;
    let kmi = content.trim();
    Some((!kmi.is_empty()).then(|| kmi.to_owned()))
}

pub fn autoload_enabled() -> bool {
    read_marker().is_some()
}

pub fn set_autoload(enabled: bool, kmi: Option<&str>) -> Result<()> {
    if !enabled {
        match fs::remove_file(SUSFS_LKM_AUTOLOAD) {
            Ok(()) => {}
            Err(e) if e.kind() == std::io::ErrorKind::NotFound => {}
            Err(e) => return Err(e).context("Failed to disable SUSFS LKM auto-load"),
        }
        println!("SUSFS LKM auto-load disabled");
        return Ok(());
    }

    ensure!(
        cfg!(target_arch = "aarch64"),
        "SUSFS guard LKM is only available for arm64"
    );

    let kmi = match kmi {
        Some(kmi) => kmi.to_owned(),
        None => get_current_kmi().context(
            "Failed to detect kernel KMI; specify it with `susfs lkm autoload enable --kmi <KMI>`",
        )?,
    };
    validate_kmi(&kmi)?;
    crate::assets::get_asset(&asset_name(&kmi))
        .with_context(|| format!("SUSFS guard LKM is unavailable for KMI {kmi}"))?;

    fs::write(SUSFS_LKM_AUTOLOAD, &kmi).context("Failed to save SUSFS LKM auto-load config")?;
    println!("SUSFS LKM auto-load enabled (KMI fallback: {kmi})");
    Ok(())
}

pub fn load(kmi: Option<&str>) -> Result<()> {
    ensure!(
        cfg!(target_arch = "aarch64"),
        "SUSFS guard LKM is only available for arm64"
    );

    if let Some(version) = detected_version() {
        println!("SUSFS {version} is already available; not loading the guard LKM");
        return Ok(());
    }

    let kmi = match kmi {
        Some(kmi) => kmi.to_owned(),
        None => get_current_kmi().context(
            "Failed to detect kernel KMI; specify it with `susfs lkm load --kmi <KMI>`",
        )?,
    };
    validate_kmi(&kmi)?;

    let name = asset_name(&kmi);
    let data = crate::assets::get_asset(&name)
        .with_context(|| format!("SUSFS guard LKM is unavailable for KMI {kmi}"))?;
    let params = CString::new("").expect("empty module params are valid");

    info!("Loading SUSFS guard LKM for KMI {kmi}");
    ksuinit::load_module(&data, &params).context("Failed to load SUSFS guard LKM")?;
    println!("Loaded SUSFS guard LKM for KMI {kmi}");
    Ok(())
}

pub fn unload() -> Result<()> {
    rustix::system::delete_module(MODULE_NAME, 0).context("Failed to unload SUSFS guard LKM")?;
    println!("Unloaded SUSFS guard LKM");
    Ok(())
}

/// Called from post-fs-data, BEFORE the persisted SUSFS config is applied.
/// Never fails the boot: errors are only logged.
pub fn load_if_autoload_enabled() {
    let Some(fallback) = read_marker() else {
        return;
    };
    if !cfg!(target_arch = "aarch64") {
        warn!("SUSFS LKM auto-load is on, but the LKM is arm64-only");
        return;
    }
    if let Some(version) = detected_version() {
        info!("SUSFS {version} already available; skipping guard LKM load");
        return;
    }
    let kmi = get_current_kmi().ok().or(fallback);
    let Some(kmi) = kmi else {
        warn!("SUSFS LKM auto-load: KMI detection failed and no fallback KMI saved");
        return;
    };
    if let Err(e) = load(Some(&kmi)) {
        warn!("SUSFS LKM auto-load failed for KMI {kmi}: {e:#}");
    }
}
