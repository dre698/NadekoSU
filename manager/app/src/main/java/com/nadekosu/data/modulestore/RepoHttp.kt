package com.nadekosu.data.modulestore

import com.nadekosu.ksuApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/** Plain-text HTTP access for repository documents, with a short in-memory cache and one retry. */
object RepoHttp {
    private const val CACHE_TTL_MS = 5 * 60 * 1000L
    private const val RETRY_DELAY_MS = 800L

    private data class Entry(val time: Long, val body: String)

    private val cache = ConcurrentHashMap<String, Entry>()

    suspend fun get(url: String, refresh: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        if (!url.isHttpUrl()) return@withContext Result.failure(IOException("unsupported url: $url"))
        cache[url]?.takeUnless { refresh }?.let {
            if (System.currentTimeMillis() - it.time < CACHE_TTL_MS) return@withContext Result.success(it.body)
        }
        var last: Throwable? = null
        for (attempt in 0..1) {
            if (attempt > 0) delay(RETRY_DELAY_MS)
            try {
                val request = Request.Builder().url(url).build()
                ksuApp.okhttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        cache[url] = Entry(System.currentTimeMillis(), body)
                        return@withContext Result.success(body)
                    }
                    if (response.code in 400..499) {
                        return@withContext Result.failure(IOException("HTTP ${response.code} for $url"))
                    }
                    last = IOException("HTTP ${response.code} for $url")
                }
            } catch (e: IOException) {
                last = e
            }
        }
        Result.failure(last ?: IOException("request failed: $url"))
    }
}

internal fun String.isHttpUrl(): Boolean =
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)
