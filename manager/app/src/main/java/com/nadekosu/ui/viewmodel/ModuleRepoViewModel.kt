package com.nadekosu.ui.viewmodel

import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadekosu.R
import com.nadekosu.data.appPreferences
import com.nadekosu.data.modulestore.DEFAULT_REPOSITORY
import com.nadekosu.data.modulestore.DEFAULT_REPOSITORY_URL
import com.nadekosu.data.modulestore.StoreModule
import com.nadekosu.data.modulestore.StoreRelease
import com.nadekosu.data.modulestore.StoreRepository
import com.nadekosu.data.modulestore.StoreSourceKind
import com.nadekosu.data.modulestore.loadClusterRepositories
import com.nadekosu.data.modulestore.loadMagiskModules
import com.nadekosu.ksuApp
import com.nadekosu.ui.activity.util.isNetworkAvailable
import com.nadekosu.ui.util.HanziToPinyin
import com.nadekosu.ui.util.module.ReleaseAssetInfo
import com.nadekosu.ui.util.module.ReleaseInfo
import com.nadekosu.ui.util.module.fetchModuleDetail
import com.nadekosu.ui.util.module.fetchModuleReadme
import com.nadekosu.ui.util.module.fetchRegistryModules
import com.nadekosu.ui.util.module.isSupportedKind
import com.topjohnwu.superuser.io.SuFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class ModuleRepoUiState(
    val modules: List<ModuleRepoViewModel.RepoModule> = emptyList(),
    val sortStargazerCountFirst: Boolean = false,
    val isRefreshing: Boolean = false,
    val search: String = "",
    val sourceKind: StoreSourceKind = StoreSourceKind.KERNELSU,
    val customUrl: String = "",
    val repositoryUrl: String = "",
    val repositories: List<StoreRepository> = emptyList(),
    val repositoriesLoading: Boolean = false,
)

class ModuleRepoViewModel : ViewModel() {
    companion object {
        private const val TAG = "ModuleRepoViewModel"
        private const val KEY_SOURCE_KIND = "module_repo_source_kind"
        private const val KEY_CUSTOM_URL = "module_repo_custom_url"
        private const val KEY_REPOSITORY_URL = "module_repo_repository_url"

        /**
         * Modules of the active non-registry source, process-wide because the detail screen builds
         * its own view model and only receives the module id. Empty while the registry is in use.
         */
        private val externalCache = ConcurrentHashMap<String, RepoModule>()

        fun isExternal(moduleId: String): Boolean = externalCache.containsKey(moduleId)
    }

    private var modulesCache: List<RepoModule> = emptyList()
    private val _uiState = MutableStateFlow(
        ModuleRepoUiState(
            sourceKind = StoreSourceKind.fromValue(ksuApp.appPreferences.getString(KEY_SOURCE_KIND)),
            customUrl = ksuApp.appPreferences.getString(KEY_CUSTOM_URL).orEmpty(),
            repositoryUrl = ksuApp.appPreferences.getString(KEY_REPOSITORY_URL).orEmpty(),
        )
    )
    val uiState: StateFlow<ModuleRepoUiState> = _uiState.asStateFlow()

    @Immutable
    @Parcelize
    data class Author(
        val name: String,
        val link: String,
    ) : Parcelable

    @Immutable
    @Parcelize
    data class RepoModule(
        val moduleId: String,
        val moduleName: String,
        val authors: String,
        val authorList: List<Author>,
        val summary: String,
        val metamodule: Boolean,
        val stargazerCount: Int,
        val updatedAt: String,
        val createdAt: String,
        val latestRelease: String,
        val latestReleaseTime: String,
        val latestVersionCode: Int,
        val latestAsset: ReleaseInfo?,
        val installed: Boolean,
        val readme: String,
        val sourceUrl: String,
        val releases: List<ReleaseInfo>
    ) : Parcelable

    fun updateSearch(search: String) {
        _uiState.update { state ->
            state.copy(
                search = search,
                modules = buildModuleList(search, state.sortStargazerCountFirst),
            )
        }
    }

    fun setSortStargazerCountFirst(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                sortStargazerCountFirst = enabled,
                modules = buildModuleList(state.search, enabled),
            )
        }
    }

    fun refresh(
        onFailure: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val netAvailable = isNetworkAvailable(ksuApp)
            _uiState.update { it.copy(isRefreshing = true) }
            val parsed = withContext(Dispatchers.IO) {
                if (!netAvailable) null else fetchForCurrentSource()
            }
            if (parsed != null) {
                modulesCache = parsed
                _uiState.update { state ->
                    state.copy(
                        modules = buildModuleList(state.search, state.sortStargazerCountFirst),
                        isRefreshing = false,
                    )
                }
            } else {
                onFailure?.invoke()
                Toast.makeText(
                    ksuApp,
                    ksuApp.getString(
                        if (netAvailable) R.string.module_repo_source_failed else R.string.network_offline
                    ),
                    Toast.LENGTH_SHORT
                ).show()
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun useKernelSu() = applySource(StoreSourceKind.KERNELSU)

    fun useCluster(repositoryUrl: String) {
        val url = repositoryUrl.trim()
        ksuApp.appPreferences.putString(KEY_REPOSITORY_URL, url)
        _uiState.update { it.copy(repositoryUrl = url) }
        applySource(StoreSourceKind.CLUSTER)
    }

    fun useCustom(url: String) {
        val value = url.trim()
        ksuApp.appPreferences.putString(KEY_CUSTOM_URL, value)
        _uiState.update { it.copy(customUrl = value) }
        applySource(StoreSourceKind.CUSTOM)
    }

    private fun applySource(kind: StoreSourceKind) {
        ksuApp.appPreferences.putString(KEY_SOURCE_KIND, kind.value)
        modulesCache = emptyList()
        externalCache.clear()
        _uiState.update { it.copy(sourceKind = kind, modules = emptyList(), search = "") }
        refresh()
    }

    /** Loads the community repository list once; the default repository is always offered. */
    fun loadRepositories() {
        if (_uiState.value.repositoriesLoading || _uiState.value.repositories.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(repositoriesLoading = true) }
            val repositories = loadClusterRepositories().getOrDefault(listOf(DEFAULT_REPOSITORY))
            _uiState.update { it.copy(repositories = repositories, repositoriesLoading = false) }
        }
    }

    private suspend fun fetchForCurrentSource(): List<RepoModule>? {
        val state = _uiState.value
        val base = when (state.sourceKind) {
            StoreSourceKind.KERNELSU -> {
                externalCache.clear()
                return fetchModulesInternal()
            }
            StoreSourceKind.CLUSTER -> state.repositoryUrl.ifBlank { DEFAULT_REPOSITORY_URL }
            StoreSourceKind.CUSTOM -> state.customUrl
        }
        if (base.isBlank()) return null
        // An empty list counts as a failure so the screen offers a retry instead of loading forever.
        val modules = loadMagiskModules(base, refresh = true)
            .onFailure { Log.e(TAG, "fetch repository failed: $base", it) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        val mapped = modules.map { it.toRepoModule() }
        externalCache.clear()
        mapped.forEach { externalCache[it.moduleId] = it }
        return mapped
    }

    private fun buildModuleList(
        search: String,
        sortStargazerCountFirst: Boolean
    ): List<RepoModule> {
        return modulesCache
            .filter { module ->
                module.moduleId.contains(search, true) ||
                        module.moduleName.contains(search, true) ||
                        HanziToPinyin.getInstance().toPinyinString(module.moduleName)
                            ?.contains(search, true) == true
            }
            .sortedWith(compareByDescending<RepoModule> { module ->
                SuFile.open("/data/adb/modules/${module.moduleId}/module.prop").exists()
            }.thenByDescending { module ->
                if (sortStargazerCountFirst) module.stargazerCount else 0
            })
    }

    /** Returns null when the registry could not be loaded (offline, HTTP error, bad format). */
    internal suspend fun fetchModulesInternal(
        onFailure: (() -> Unit)? = null
    ): List<RepoModule>? {
        return runCatching {
            fetchRegistryModules()
                .filter { isSupportedKind(it.optString("kind", "module")) }
                .mapNotNull { parseRepoModuleSummary(it) }
        }.onFailure {
            Log.e(TAG, "fetch modules failed", it)
            onFailure?.invoke()
        }.getOrNull()
    }

    /** Builds a list entry from modules.json only; releases/readme are loaded lazily. */
    private fun parseRepoModuleSummary(item: JSONObject): RepoModule? {
        val moduleId = item.optString("id", "")
        if (moduleId.isEmpty()) return null
        val authorsArray = item.optJSONArray("authors")
        val authorList = if (authorsArray != null) {
            (0 until authorsArray.length()).mapNotNull { idx ->
                val a = authorsArray.optJSONObject(idx) ?: return@mapNotNull null
                val name = a.optString("name", "").trim()
                if (name.isEmpty()) null else Author(name, a.optString("url", "").trim())
            }
        } else emptyList()
        val stable = item.optJSONObject("stable")
        return RepoModule(
            moduleId = moduleId,
            moduleName = item.optString("name", "").trim().ifBlank { moduleId },
            authors = authorList.joinToString(", ") { it.name },
            authorList = authorList,
            summary = item.optString("summary", "").trim(),
            metamodule = item.optBoolean("metamodule", false),
            stargazerCount = item.optInt("stars", 0),
            updatedAt = item.optString("updatedAt", ""),
            createdAt = "",
            latestRelease = stable?.optString("version", "").orEmpty(),
            latestReleaseTime = stable?.optString("publishedAt", "").orEmpty(),
            latestVersionCode = stable?.optInt("versionCode", 0) ?: 0,
            latestAsset = null,
            installed = SuFile.open("/data/adb/modules/${moduleId}/module.prop").exists(),
            readme = "",
            sourceUrl = "",
            releases = emptyList(),
        )
    }

    /** Loads releases, changelog and readme of a module (one request + one for the readme). */
    suspend fun loadDetail(module: RepoModule): RepoModule? = withContext(Dispatchers.IO) {
        if (isExternal(module.moduleId)) return@withContext module
        val detail = fetchModuleDetail(module.moduleId) ?: return@withContext null
        val full = module.copy(
            moduleName = detail.name.trim().ifBlank { module.moduleName },
            summary = detail.summary.trim().ifBlank { module.summary },
            latestAsset = detail.latestRelease,
            releases = detail.releases,
            readme = fetchModuleReadme(detail.readmePath),
            sourceUrl = detail.sourceUrl.orEmpty(),
        )
        modulesCache = modulesCache.map { if (it.moduleId == full.moduleId) full else it }
        _uiState.update { state ->
            state.copy(modules = buildModuleList(state.search, state.sortStargazerCountFirst))
        }
        full
    }

    /** Detail screen: the nav route only carries the id, so build the full module from its detail. */
    suspend fun fetchById(moduleId: String): RepoModule? = withContext(Dispatchers.IO) {
        externalCache[moduleId]?.let { return@withContext it }
        val detail = fetchModuleDetail(moduleId) ?: return@withContext null
        if (!isSupportedKind(detail.kind)) return@withContext null
        RepoModule(
            moduleId = moduleId,
            moduleName = detail.name.trim().ifBlank { moduleId },
            authors = detail.authors.joinToString(", ") { it.first },
            authorList = detail.authors.map { Author(it.first, it.second) },
            summary = detail.summary.trim(),
            metamodule = detail.metamodule,
            stargazerCount = detail.stars,
            updatedAt = detail.updatedAt,
            createdAt = "",
            latestRelease = detail.stable.version,
            latestReleaseTime = detail.stable.publishedAt,
            latestVersionCode = detail.stable.versionCode,
            latestAsset = detail.latestRelease,
            installed = SuFile.open("/data/adb/modules/${moduleId}/module.prop").exists(),
            readme = fetchModuleReadme(detail.readmePath),
            sourceUrl = detail.sourceUrl.orEmpty(),
            releases = detail.releases,
        )
    }
}

private fun StoreModule.toRepoModule(): ModuleRepoViewModel.RepoModule {
    val releaseInfos = releases.sortedByDescending { it.versionCode }.map { it.toReleaseInfo() }
    val latest = latestRelease
    val latestInfo = latest?.toReleaseInfo()
    return ModuleRepoViewModel.RepoModule(
        moduleId = id,
        moduleName = name,
        authors = author,
        authorList = if (author.isBlank()) emptyList() else listOf(ModuleRepoViewModel.Author(author, "")),
        summary = description,
        metamodule = false,
        stargazerCount = 0,
        updatedAt = latestInfo?.publishedAt.orEmpty(),
        createdAt = "",
        latestRelease = latest?.version ?: version,
        latestReleaseTime = latestInfo?.publishedAt.orEmpty(),
        latestVersionCode = (latest?.versionCode ?: versionCode).toInt(),
        latestAsset = latestInfo,
        installed = SuFile.open("/data/adb/modules/$id/module.prop").exists(),
        readme = "",
        sourceUrl = source ?: homepage.orEmpty(),
        releases = releaseInfos,
    )
}

private fun StoreRelease.toReleaseInfo(): ReleaseInfo = ReleaseInfo(
    name = version,
    tagName = version,
    publishedAt = formatStoreTimestamp(timestamp),
    descriptionHTML = changelogToHtml(changelog),
    assets = listOf(
        ReleaseAssetInfo(
            name = downloadUrl.substringBefore('?').substringAfterLast('/').ifBlank { "$version.zip" },
            downloadUrl = downloadUrl,
            // Magisk-format repositories do not publish file sizes or download counts.
            size = 0L,
            downloadCount = 0,
        )
    ),
)

/** Accepts epoch seconds or milliseconds; returns an empty string when there is no timestamp. */
private fun formatStoreTimestamp(timestamp: Double): String {
    if (timestamp <= 0.0) return ""
    val millis = if (timestamp > 1e12) timestamp.toLong() else (timestamp * 1000).toLong()
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
}

/** Magisk changelogs are usually a link to a text file, so a bare URL becomes a clickable link. */
private fun changelogToHtml(changelog: String): String {
    val text = changelog.trim()
    if (text.isEmpty()) return ""
    val escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return if (text.startsWith("http://") || text.startsWith("https://")) {
        "<a href=\"$escaped\">$escaped</a>"
    } else {
        escaped.replace("\n", "<br>")
    }
}
