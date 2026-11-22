package com.nadekosu.data.modulestore

import org.json.JSONArray
import org.json.JSONObject

/** Repository used when the cluster index is unavailable or does not list it. */
const val DEFAULT_REPOSITORY_URL = "https://gr.dergoogler.com/gmr/"

val DEFAULT_REPOSITORY = StoreRepository(name = "GMR", url = DEFAULT_REPOSITORY_URL)

private const val CLUSTER_INDEX_URL = "https://mmrl.dev/api/repositories.json"

/** A base that already ends in `.json` is used as is; otherwise `<base>/json/modules.json`. */
fun resolveRepoModulesUrl(repoBase: String): String {
    val base = repoBase.trim()
    return if (base.endsWith(".json", ignoreCase = true)) base else base.trimEnd('/') + "/json/modules.json"
}

private fun resolveRepoBaseUrl(repoBase: String): String {
    val base = repoBase.trim()
    val directory = if (base.endsWith(".json", ignoreCase = true)) base.substringBeforeLast('/') else base
    return directory.trimEnd('/') + "/"
}

private fun resolveDownloadUrl(repoBase: String, zipUrl: String): String {
    val raw = zipUrl.trim()
    return if (raw.isHttpUrl()) raw else resolveRepoBaseUrl(repoBase) + raw.trimStart('/')
}

/** Loads and parses a Magisk-format module list. */
suspend fun loadMagiskModules(repoBase: String, refresh: Boolean = false): Result<List<StoreModule>> {
    val url = resolveRepoModulesUrl(repoBase)
    if (!url.isHttpUrl()) return Result.failure(IllegalArgumentException("invalid repository url: $repoBase"))
    return RepoHttp.get(url, refresh).mapCatching { parseMagiskModules(it, repoBase) }
}

/** Loads the public cluster index of community repositories, always including the default one. */
suspend fun loadClusterRepositories(): Result<List<StoreRepository>> =
    RepoHttp.get(CLUSTER_INDEX_URL).mapCatching { body ->
        val repositories = parseRepositories(body)
        if (repositories.any { it.url == DEFAULT_REPOSITORY_URL }) repositories
        else listOf(DEFAULT_REPOSITORY) + repositories
    }

internal fun parseRepositories(json: String): List<StoreRepository> {
    val array = JSONArray(json)
    val repositories = ArrayList<StoreRepository>(array.length())
    for (index in 0 until array.length()) {
        val item = array.optJSONObject(index) ?: continue
        val url = item.optString("url", "").trim()
        if (url.isEmpty()) continue
        repositories += StoreRepository(
            name = item.optString("name", ""),
            url = url,
            description = item.optString("description", ""),
            modulesCount = item.optInt("modules_count", 0),
        )
    }
    return repositories
}

internal fun parseMagiskModules(json: String, repoBase: String): List<StoreModule> {
    val array = JSONObject(json).optJSONArray("modules") ?: return emptyList()
    val modules = ArrayList<StoreModule>(array.length())
    for (index in 0 until array.length()) {
        val item = array.optJSONObject(index) ?: continue
        val name = item.optString("name", "").trim()
        val id = item.optString("id", "").trim().ifEmpty { name }
        if (id.isEmpty()) continue
        val track = item.optJSONObject("track")
        val releases = parseReleases(item.optJSONArray("versions"), repoBase)
        val versionCode = item.optLong("versionCode", 0L)
        val latest = releases.firstOrNull { it.versionCode == versionCode }
            ?: releases.maxByOrNull { it.versionCode }
        modules += StoreModule(
            id = id,
            name = name.ifEmpty { id },
            version = item.optString("version", "").ifEmpty { latest?.version.orEmpty() },
            versionCode = versionCode,
            author = item.optString("author", ""),
            description = item.optString("description", ""),
            license = trackValue(track, item, "license"),
            homepage = trackValue(track, item, "homepage"),
            source = trackValue(track, item, "source"),
            support = trackValue(track, item, "support"),
            releases = releases,
        )
    }
    return modules
}

private fun parseReleases(versions: JSONArray?, repoBase: String): List<StoreRelease> {
    if (versions == null) return emptyList()
    val releases = ArrayList<StoreRelease>(versions.length())
    for (index in 0 until versions.length()) {
        val version = versions.optJSONObject(index) ?: continue
        val zipUrl = version.optString("zipUrl", "").trim()
        if (zipUrl.isEmpty()) continue
        releases += StoreRelease(
            version = version.optString("version", ""),
            versionCode = version.optLong("versionCode", 0L),
            downloadUrl = resolveDownloadUrl(repoBase, zipUrl),
            changelog = version.optString("changelog", ""),
            timestamp = version.optDouble("timestamp", 0.0),
        )
    }
    return releases
}

private fun trackValue(track: JSONObject?, module: JSONObject, key: String): String? {
    val tracked = track?.optString(key, "")?.takeIf { it.isNotBlank() }
    if (tracked != null) return tracked
    return module.optString(key, "").takeIf { it.isNotBlank() }
}
