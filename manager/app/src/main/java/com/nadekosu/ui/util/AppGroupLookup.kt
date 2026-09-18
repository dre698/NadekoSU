package com.nadekosu.ui.util

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.nadekosu.Natives
import com.nadekosu.ksuApp
import com.nadekosu.ui.KsuService
import com.nadekosu.ui.viewmodel.ModuleRepoViewModel
import com.nadekosu.ui.viewmodel.SuperUserViewModel
import com.nadekosu.zako.IKsuInterface
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "AppGroupLookup"

/**
 * Same KsuService root-bind used by SuperUserViewModel's app list loading, extracted here so
 * a single-app lookup (see [fetchAppGroupForUid]) doesn't need a SuperUserViewModel instance.
 */
suspend fun connectKsuServiceForLookup(onDisconnect: () -> Unit = {}): IBinder? =
    suspendCancellableCoroutine { continuation ->
        val connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                onDisconnect()
            }

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                continuation.resume(binder)
            }
        }
        val intent = Intent(ksuApp, KsuService::class.java)
        try {
            val task = com.topjohnwu.superuser.ipc.RootService.bindOrTask(
                intent, Shell.EXECUTOR, connection
            )
            task?.let { Shell.getShell().execTask(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind KsuService", e)
            continuation.resume(null)
        }
    }

/**
 * Rebuilds a single [SuperUserViewModel.AppGroup] for [uid] via the KSU root service, the way
 * SuperUserViewModel.groupAppsByUid does for the whole app list - used by AppProfileScreen,
 * which (since the nav backstack route only carries uid + packageName, not the full AppGroup
 * object) needs to look the group back up rather than receive it directly.
 *
 * [preferredPackageName] is used only to order [AppGroup.apps] so the app the user actually
 * tapped ends up first (mirrors) when a uid is shared by several packages.
 */
suspend fun fetchAppGroupForUid(uid: Int, preferredPackageName: String): SuperUserViewModel.AppGroup? {
    val binder = connectKsuServiceForLookup() ?: return null

    val pm = ksuApp.packageManager
    val allPackages = IKsuInterface.Stub.asInterface(binder)
    val total = allPackages.packageCount
    val pageSize = 100
    val matches = mutableListOf<SuperUserViewModel.AppInfo>()

    var start = 0
    while (start < total) {
        val page = allPackages.getPackages(start, pageSize)
        if (page.isEmpty()) break

        for (packageInfo in page) {
            val appInfo = packageInfo.applicationInfo ?: continue
            if (appInfo.uid != uid) continue
            matches += SuperUserViewModel.AppInfo(
                label = appInfo.loadLabel(pm).toString(),
                packageInfo = packageInfo,
                profile = Natives.getAppProfile(packageInfo.packageName, uid)
            )
        }

        start += pageSize
    }

    if (matches.isEmpty()) return null

    val sortedApps = matches.sortedWith(
        compareBy(
            { it.packageName != preferredPackageName },
            { it.label }
        )
    )
    val profile = sortedApps.firstOrNull()?.let { Natives.getAppProfile(it.packageName, uid) }

    return SuperUserViewModel.AppGroup(uid = uid, apps = sortedApps, profile = profile)
}

/**
 * Looks up a single online repo module by id, for OnlineModuleDetailScreen (the nav route only
 * carries the id, not the full RepoModule object). ModuleRepoViewModel.fetchModulesInternal
 * doesn't touch any ViewModel/lifecycle state internally, so calling it on a bare instance here
 * (bypassing the Compose viewModel() factory) is safe - it re-fetches the whole modules.json,
 * same as ModuleRepo's own list screen does, since there's no dedicated single-module endpoint.
 */
suspend fun fetchRepoModuleById(moduleId: String): ModuleRepoViewModel.RepoModule? =
    ModuleRepoViewModel().fetchModulesInternal().find { it.moduleId == moduleId }
