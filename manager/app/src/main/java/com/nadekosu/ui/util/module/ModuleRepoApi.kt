package com.nadekosu.ui.util.module

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Immutable
import com.nadekosu.ksuApp
import com.nadekosu.ui.activity.util.isNetworkAvailable
import kotlinx.parcelize.Parcelize
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

const val TAG = "ModuleRepoApi"

/** Site root of the KernelSU Modules registry (API v1). */
const val REGISTRY_BASE_URL = "https://modules.kernelsu.org/"
const val REGISTRY_INDEX_PATH = "api/v1/index.json"
private const val REGISTRY_SCHEMA_VERSION = 1
private const val MAX_DOCUMENT_BYTES = 16L * 1024 * 1024
private val API_PATH = Regex("^[A-Za-z0-9._\\-/]+$")
private val DOWNLOAD_HOSTS = setOf(
    "github.com",
    "objects.githubusercontent.com",
    "release-assets.githubusercontent.com",
)

class RegistryException(message: String) : Exception(message)

data class ModuleDetail(
    val readmePath: String?,
    val stable: ModuleStable,
    val releases: List<ReleaseInfo>,
    val latestRelease: ReleaseInfo?,
    val sourceUrl: String?,
    val homepageUrl: String?,
    val name: String,
    val summary: String,
    val authors: List<Pair<String, String>>,
    val stars: Int,
    val updatedAt: String,
    val metamodule: Boolean,
    val kind: String,
)

data class ModuleStable(val version: String, val versionCode: Int, val publishedAt: String)

/** Fetches a document of the registry. Only relative, sanitized paths are accepted. */
fun registryFetch(path: String): String {
    if (path.isEmpty() || path.startsWith("/") || path.contains("..") || !API_PATH.matches(path)) {
        throw RegistryException("Invalid registry path: $path")
    }
    if (!isNetworkAvailable(ksuApp)) throw RegistryException("offline")
    val request = Request.Builder().url(REGISTRY_BASE_URL + path).build()
    ksuApp.okhttpClient.newCall(request).execute().use { resp ->
        if (!resp.isSuccessful) throw RegistryException("HTTP ${resp.code} for $path")
        val body = resp.body ?: throw RegistryException("Empty response for $path")
        if (body.contentLength() > MAX_DOCUMENT_BYTES) throw RegistryException("Document too large")
        return body.string()
    }
}

private fun checkSchema(obj: JSONObject) {
    val v = obj.optInt("schemaVersion", -1)
    if (v != REGISTRY_SCHEMA_VERSION) throw RegistryException("Unsupported schemaVersion: $v")
}

/** index.json -> modules.json (retried once when both documents come from different builds). */
fun fetchRegistryModules(): List<JSONObject> {
    var last: Exception? = null
    repeat(2) {
        try {
            val index = JSONObject(registryFetch(REGISTRY_INDEX_PATH)).also(::checkSchema)
            val modulesPath = index.optString("modules", "api/v1/modules.json")
            val doc = JSONObject(registryFetch(modulesPath)).also(::checkSchema)
            if (doc.optString("buildId") != index.optString("buildId")) {
                throw RegistryException("build mismatch")
            }
            val arr = doc.optJSONArray("modules") ?: JSONArray()
            return (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }
        } catch (e: Exception) {
            last = e
        }
    }
    throw last ?: RegistryException("unknown")
}

private fun isAllowedDownloadUrl(url: String): Boolean {
    val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
    return uri.scheme.equals("https", true) && uri.host?.lowercase() in DOWNLOAD_HOSTS && uri.rawUserInfo == null
}

/** Whether the manager can handle this registry entry (scripts are not installable here). */
fun isSupportedKind(kind: String): Boolean = kind == "module"

@Immutable
@Parcelize
data class ReleaseInfo(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val descriptionHTML: String,
    val assets: List<ReleaseAssetInfo>
) : Parcelable

@Immutable
@Parcelize
data class ReleaseAssetInfo(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int
) : Parcelable

fun fetchModuleDetail(moduleId: String): ModuleDetail? {
    return runCatching {
        val obj = JSONObject(registryFetch("api/v1/modules/$moduleId.json")).also(::checkSchema)
        if (obj.optString("id") != moduleId) throw RegistryException("Detail id mismatch")

        val stableObj = obj.optJSONObject("stable")
        val stable = ModuleStable(
            version = stableObj?.optString("version", "").orEmpty(),
            versionCode = stableObj?.optInt("versionCode", 0) ?: 0,
            publishedAt = stableObj?.optString("publishedAt", "").orEmpty(),
        )

        val releasesArray = obj.optJSONArray("releases") ?: JSONArray()
        val releases = (0 until releasesArray.length()).mapNotNull { idx ->
            val r = releasesArray.optJSONObject(idx) ?: return@mapNotNull null
            if (r.optBoolean("prerelease", false)) return@mapNotNull null
            val zip = r.optJSONObject("zip") ?: return@mapNotNull null
            val zipUrl = zip.optString("url", "")
            if (!isAllowedDownloadUrl(zipUrl)) return@mapNotNull null
            val tag = r.optString("tag", "")
            val version = r.optString("version", tag)
            val name = r.optString("name", "").ifBlank { version.ifBlank { tag } }
            val zipName = zip.optString("name", "").substringAfterLast('/')
                .ifBlank { "${moduleId}-${r.optInt("versionCode", 0)}.zip" }
            Pair(
                r.optInt("versionCode", 0),
                ReleaseInfo(
                    name = name,
                    tagName = tag,
                    publishedAt = r.optString("publishedAt", ""),
                    descriptionHTML = r.optString("changelogHtml", ""),
                    assets = listOf(ReleaseAssetInfo(zipName, zipUrl, zip.optLong("size", 0L), 0)),
                )
            )
        }

        val authorsArray = obj.optJSONArray("authors") ?: JSONArray()
        val authors = (0 until authorsArray.length()).mapNotNull { idx ->
            val a = authorsArray.optJSONObject(idx) ?: return@mapNotNull null
            val n = a.optString("name", "").trim()
            if (n.isEmpty()) null else Pair(n, a.optString("url", "").trim())
        }

        ModuleDetail(
            readmePath = obj.optString("readme", "").ifBlank { null },
            stable = stable,
            releases = releases.map { it.second },
            latestRelease = releases.firstOrNull { it.first == stable.versionCode }?.second
                ?: releases.firstOrNull()?.second,
            sourceUrl = obj.optJSONObject("source")?.optString("url", "")?.ifBlank { null },
            homepageUrl = obj.optString("homepage", "").ifBlank { null },
            name = obj.optString("name", ""),
            summary = obj.optString("summary", ""),
            authors = authors,
            stars = obj.optInt("stars", 0),
            updatedAt = obj.optString("updatedAt", ""),
            metamodule = obj.optBoolean("metamodule", false),
            kind = obj.optString("kind", "module"),
        )
    }.getOrElse {
        Log.e(TAG, "Module Repo Api Calling FAILED: $moduleId", it)
        null
    }
}

/** Readme is an HTML fragment at api/v1/modules/{id}/readme; empty when the module has none. */
fun fetchModuleReadme(path: String?): String {
    if (path == null) return ""
    return runCatching { registryFetch(path) }.getOrElse {
        Log.e(TAG, "Readme fetch failed: $path", it)
        ""
    }
}
