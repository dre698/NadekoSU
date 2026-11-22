package com.nadekosu.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Keeps the Dashboard UI banner image in app-private storage.
 * Every pick gets a new file name so image caches never serve a stale banner.
 */
object HomeBannerStorage {
    private const val DIR = "home_banner"
    private const val MAX_EDGE = 1600

    fun fileOf(context: Context, name: String?): File? {
        if (name.isNullOrBlank()) return null
        return File(File(context.filesDir, DIR), name).takeIf { it.isFile }
    }

    fun delete(context: Context, name: String?) {
        if (name.isNullOrBlank()) return
        runCatching { File(File(context.filesDir, DIR), name).delete() }
    }

    /** Decodes, downsizes and stores [uri]. Returns the new file name, or null on failure. */
    suspend fun import(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            val rotation = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val bitmap = if (rotation != 0f) {
                Bitmap.createBitmap(
                    decoded, 0, 0, decoded.width, decoded.height,
                    Matrix().apply { postRotate(rotation) }, true
                )
            } else decoded

            val dir = File(context.filesDir, DIR).apply { mkdirs() }
            val name = "banner_${System.currentTimeMillis()}.jpg"
            File(dir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            name
        }.getOrNull()
    }
}
