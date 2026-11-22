package com.nadekosu.data.modulestore

/** Where the module repo screen reads its list from. Persisted values are stable tokens. */
enum class StoreSourceKind(val value: String) {
    /** The built-in KernelSU module registry (the original behaviour). */
    KERNELSU("kernelsu"),

    /** A community repository picked from the public cluster index. */
    CLUSTER("mmrl"),

    /** A repository URL typed in by the user, in the Magisk module list format. */
    CUSTOM("custom");

    companion object {
        fun fromValue(value: String?): StoreSourceKind =
            entries.firstOrNull { it.value == value } ?: KERNELSU
    }
}

/** One downloadable version of a [StoreModule]. */
data class StoreRelease(
    val version: String,
    val versionCode: Long,
    val downloadUrl: String,
    val changelog: String = "",
    val timestamp: Double = 0.0,
)

/** A module entry normalised from a Magisk-format repository. */
data class StoreModule(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Long,
    val author: String,
    val description: String,
    val license: String? = null,
    val homepage: String? = null,
    val source: String? = null,
    val support: String? = null,
    val releases: List<StoreRelease> = emptyList(),
) {
    /** The release matching [versionCode], else the highest version code with a download URL. */
    val latestRelease: StoreRelease?
        get() {
            val downloadable = releases.filter { it.downloadUrl.isNotBlank() }
            if (downloadable.isEmpty()) return null
            return downloadable.firstOrNull { it.versionCode == versionCode }
                ?: downloadable.maxByOrNull { it.versionCode }
        }
}

/** A community repository advertised by the cluster index. */
data class StoreRepository(
    val name: String,
    val url: String,
    val description: String = "",
    val modulesCount: Int = 0,
)
