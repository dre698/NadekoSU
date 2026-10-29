package com.nadekosu.ui.viewmodel

import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadekosu.R
import com.nadekosu.ksuApp
import com.nadekosu.ui.activity.util.isNetworkAvailable
import com.nadekosu.ui.util.HanziToPinyin
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

data class ModuleRepoUiState(
    val modules: List<ModuleRepoViewModel.RepoModule> = emptyList(),
    val sortStargazerCountFirst: Boolean = false,
    val isRefreshing: Boolean = false,
    val search: String = "",
)

class ModuleRepoViewModel : ViewModel() {
    companion object {
        private const val TAG = "ModuleRepoViewModel"
    }

    private var modulesCache: List<RepoModule> = emptyList()
    private val _uiState = MutableStateFlow(ModuleRepoUiState())
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
                if (!netAvailable) null else fetchModulesInternal()
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
                    ksuApp.getString(R.string.network_offline),
                    Toast.LENGTH_SHORT
                ).show()
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
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
