use anyhow::{Context, Result, bail};
use clap::Subcommand;
use log::{info, warn};

use const_format::concatcp;
use prop_rs_android::resetprop::ResetProp;
use prop_rs_android::sys_prop;
use std::fs::{File, OpenOptions, Permissions};
use std::io::Write;
use std::os::unix::fs::{OpenOptionsExt, PermissionsExt};
use std::path::Path;

const PROPERTIES: [(&str, &str); 16] = [
    ("ro.boot.vbmeta.device_state", "locked"),
    ("ro.boot.verifiedbootstate", "green"),
    ("ro.boot.flash.locked", "1"),
    ("ro.boot.veritymode", "enforcing"),
    ("vendor.boot.vbmeta.device_state", "locked"),
    ("vendor.boot.verifiedbootstate", "green"),
    ("ro.boot.warranty_bit", "0"),
    ("ro.warranty_bit", "0"),
    ("ro.vendor.boot.warranty_bit", "0"),
    ("ro.vendor.warranty_bit", "0"),
    ("ro.secureboot.lockstate", "locked"),
    ("ro.boot.realmebootstate", "green"),
    ("ro.boot.realme.lockstate", "1"),
    ("ro.debuggable", "0"),
    ("ro.adb.secure", "1"),
    ("ro.secure", "1"),
];

const MARKER_PATH: &str = concatcp!(crate::defs::WORKING_DIR, ".boot_hide");

#[derive(Debug, Clone, Subcommand)]
pub enum Command {
    Status,
    Enable,
    Disable,
}

fn apply_with(
    mut get: impl FnMut(&str) -> Option<String>,
    mut set: impl FnMut(&str, &str) -> bool,
    mut delete: impl FnMut(&str),
) -> usize {
    let mut writes = 0;
    for &(key, value) in &PROPERTIES {
        let Some(current) = get(key) else {
            continue;
        };
        if current != value {
            set(key, value);
            writes += 1;
        }
    }
    if get("service.adb.root").is_some() {
        delete("service.adb.root");
    }
    writes
}

fn marker_path() -> &'static Path {
    Path::new(MARKER_PATH)
}

fn marker_exists() -> Result<bool> {
    marker_path()
        .try_exists()
        .context("Inspect Nadeko Hide marker")
}

fn write_marker() -> Result<()> {
    let path = marker_path();
    let mut file = OpenOptions::new()
        .write(true)
        .create(true)
        .truncate(true)
        .mode(0o600)
        .custom_flags(libc::O_NOFOLLOW)
        .open(path)
        .with_context(|| format!("Open {}", path.display()))?;
    file.set_permissions(Permissions::from_mode(0o600))
        .context("Set Nadeko Hide marker permissions")?;
    file.flush().context("Write Nadeko Hide marker")?;
    file.sync_all().context("Sync Nadeko Hide marker")?;
    File::open(crate::defs::WORKING_DIR)
        .context("Open working directory")?
        .sync_all()
        .context("Sync working directory")?;
    Ok(())
}

fn remove_marker() -> Result<()> {
    match std::fs::remove_file(marker_path()) {
        Ok(()) => {}
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => return Ok(()),
        Err(error) => return Err(error).context("Remove Nadeko Hide marker"),
    }
    File::open(crate::defs::WORKING_DIR)
        .context("Open working directory")?
        .sync_all()
        .context("Sync working directory")?;
    Ok(())
}

fn rebuild_if_needed(rp: &ResetProp, key: &str, needed: bool) -> Result<()> {
    if needed {
        let context = sys_prop::get_context(key)
            .with_context(|| format!("Get property context for {key}"))?;
        rp.rebuild(&context)
            .with_context(|| format!("Rebuild property area for {key}"))?;
    }
    Ok(())
}

fn apply() -> Result<usize> {
    sys_prop::init().context("Initialize system property API")?;
    let rp = ResetProp {
        skip_svc: true,
        persistent: false,
        persist_only: false,
        verbose: false,
        show_context: false,
        rebuild: false,
    };

    let mut set_error = None;
    let mut delete_error = None;
    let writes = apply_with(
        sys_prop::get,
        |key, value| {
            if set_error.is_some() {
                return false;
            }
            match rp
                .set(key, value)
                .with_context(|| format!("Set {key}"))
                .and_then(|needed| rebuild_if_needed(&rp, key, needed))
            {
                Ok(()) => true,
                Err(error) => {
                    set_error = Some(error);
                    false
                }
            }
        },
        |key| {
            if delete_error.is_some() {
                return;
            }
            if let Err(error) = rp
                .delete(key)
                .with_context(|| format!("Delete {key}"))
                .and_then(|needed| rebuild_if_needed(&rp, key, needed))
            {
                delete_error = Some(error);
            }
        },
    );

    if let Some(error) = set_error.or(delete_error) {
        return Err(error);
    }
    Ok(writes)
}

fn ensure_root() -> Result<()> {
    if unsafe { libc::geteuid() } != 0 {
        bail!("Root is required for Nadeko Hide");
    }
    Ok(())
}

pub fn boot_apply() {
    match marker_exists() {
        Ok(false) => return,
        Ok(true) => {}
        Err(error) => {
            warn!("Inspect Nadeko Hide marker failed: {error:#}");
            return;
        }
    }
    if crate::android::utils::is_safe_mode() {
        warn!("safe mode, skip Nadeko Hide");
        return;
    }
    match apply() {
        Ok(writes) => info!("Nadeko Hide applied {writes} properties"),
        Err(error) => warn!("Apply Nadeko Hide failed: {error:#}"),
    }
}

pub fn run(command: &Command) -> Result<()> {
    ensure_root()?;
    match command {
        Command::Status => {
            println!(
                "{}",
                if marker_exists()? {
                    "enabled"
                } else {
                    "disabled"
                }
            );
            Ok(())
        }
        Command::Enable => {
            write_marker()?;
            if let Err(error) = apply() {
                warn!("Apply Nadeko Hide failed: {error:#}");
            }
            Ok(())
        }
        Command::Disable => remove_marker(),
    }
}

#[cfg(test)]
#[path = "boot_hide_tests.rs"]
mod tests;
