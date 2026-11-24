package com.nadekosu.ui.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.nadekosu.data.HomeLayoutStyle
import com.nadekosu.ui.theme.ThemeSeedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Everything a theme package carries besides the images. */
data class ThemePackage(
    /** 0 = follow system, 1 = light, 2 = dark. */
    val themeMode: Int = 0,
    val useDynamicColor: Boolean = false,
    val seedColor: Int = ThemeSeedColors.Default.toArgb(),
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
    val cardAlpha: Float = 1f,
    val moduleBannerAlpha: Float = 0.75f,
    val backgroundDim: Float = 0f,
    val enableBlur: Boolean = false,
    val enableBlurExp: Boolean = false,
    val enableLiquidGlass: Boolean = false,
    val useBackgroundSeedColor: Boolean = false,
    val highContrast: Boolean = false,
    val homeLayoutStyle: String = HomeLayoutStyle.DEFAULT,
    val showHomeCardIcons: Boolean = false,
    val showNavigationBarBadge: Boolean = true,
)

/** A parsed package; the image files live in the cache until the apply step has copied them. */
class ImportedTheme(
    val data: ThemePackage,
    val backgroundFile: File?,
    val bannerFile: File?,
)

/**
 * Plain zip: `theme.json` (settings) + optional `background` and `banner` images.
 * Entries are only ever read by their fixed names and written to fixed locations, so a package
 * cannot place files anywhere else.
 */
object ThemePackageStorage {
    const val MIME_TYPE = "application/zip"

    private const val FORMAT = "nadeko-theme"
    private const val VERSION = 1
    private const val MANIFEST_ENTRY = "theme.json"
    private const val BACKGROUND_ENTRY = "background"
    private const val BANNER_ENTRY = "banner"
    private const val MAX_JSON_BYTES = 64L * 1024
    private const val MAX_IMAGE_BYTES = 32L * 1024 * 1024
    private const val IMPORT_DIR = "theme_import"

    fun packageFileName(): String =
        "nadeko-theme-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date()) + ".zip"

    suspend fun export(
        context: Context,
        outputUri: Uri,
        data: ThemePackage,
        backgroundUri: Uri?,
        bannerFile: File?,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val out = context.contentResolver.openOutputStream(outputUri, "wt")
                ?: throw IOException("Cannot open output")
            ZipOutputStream(out.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
                zip.write(toJson(data).toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                if (backgroundUri != null) {
                    context.contentResolver.openInputStream(backgroundUri)?.use { input ->
                        zip.putNextEntry(ZipEntry(BACKGROUND_ENTRY))
                        input.copyTo(zip)
                        zip.closeEntry()
                    }
                }
                if (bannerFile != null && bannerFile.isFile) {
                    zip.putNextEntry(ZipEntry(BANNER_ENTRY))
                    bannerFile.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }

    suspend fun import(context: Context, uri: Uri): Result<ImportedTheme> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.cacheDir, IMPORT_DIR).apply {
                    deleteRecursively()
                    mkdirs()
                }
                var manifest: ThemePackage? = null
                var background: File? = null
                var banner: File? = null

                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Cannot open package")
                ZipInputStream(input.buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            MANIFEST_ENTRY -> {
                                val text = readLimited(zip, MAX_JSON_BYTES).toString(Charsets.UTF_8)
                                manifest = fromJson(text)
                            }

                            BACKGROUND_ENTRY ->
                                background = File(dir, "background.img")
                                    .also { writeLimited(zip, it, MAX_IMAGE_BYTES) }
                                    .takeIf { it.isDecodableImage() }

                            BANNER_ENTRY ->
                                banner = File(dir, "banner.img")
                                    .also { writeLimited(zip, it, MAX_IMAGE_BYTES) }
                                    .takeIf { it.isDecodableImage() }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }

                val data = manifest ?: throw IOException("Not a Nadeko theme package")
                ImportedTheme(data, background, banner)
            }
        }

    private fun toJson(data: ThemePackage): JSONObject = JSONObject()
        .put("format", FORMAT)
        .put("version", VERSION)
        .put("themeMode", data.themeMode)
        .put("useDynamicColor", data.useDynamicColor)
        .put("seedColor", data.seedColor)
        .put("paletteStyle", data.paletteStyle.name)
        .put("colorSpec", data.colorSpec.name)
        .put("cardAlpha", data.cardAlpha.toDouble())
        .put("moduleBannerAlpha", data.moduleBannerAlpha.toDouble())
        .put("backgroundDim", data.backgroundDim.toDouble())
        .put("enableBlur", data.enableBlur)
        .put("enableBlurExp", data.enableBlurExp)
        .put("enableLiquidGlass", data.enableLiquidGlass)
        .put("useBackgroundSeedColor", data.useBackgroundSeedColor)
        .put("highContrast", data.highContrast)
        .put("homeLayoutStyle", data.homeLayoutStyle)
        .put("showHomeCardIcons", data.showHomeCardIcons)
        .put("showNavigationBarBadge", data.showNavigationBarBadge)

    private fun fromJson(text: String): ThemePackage {
        val json = JSONObject(text)
        if (json.optString("format") != FORMAT) throw IOException("Not a Nadeko theme package")
        if (json.optInt("version", 0) > VERSION) throw IOException("Package is from a newer version")
        val defaults = ThemePackage()
        return ThemePackage(
            themeMode = json.optInt("themeMode", defaults.themeMode).coerceIn(0, 2),
            useDynamicColor = json.optBoolean("useDynamicColor", defaults.useDynamicColor),
            seedColor = json.optInt("seedColor", defaults.seedColor),
            paletteStyle = PaletteStyle.entries
                .firstOrNull { it.name == json.optString("paletteStyle") }
                ?: defaults.paletteStyle,
            colorSpec = ColorSpec.SpecVersion.entries
                .firstOrNull { it.name == json.optString("colorSpec") }
                ?: defaults.colorSpec,
            cardAlpha = json.optDouble("cardAlpha", defaults.cardAlpha.toDouble()).toFloat().coerceIn(0f, 1f),
            moduleBannerAlpha = json.optDouble("moduleBannerAlpha", defaults.moduleBannerAlpha.toDouble())
                .toFloat().coerceIn(0f, 1f),
            backgroundDim = json.optDouble("backgroundDim", defaults.backgroundDim.toDouble())
                .toFloat().coerceIn(0f, 1f),
            enableBlur = json.optBoolean("enableBlur", defaults.enableBlur),
            enableBlurExp = json.optBoolean("enableBlurExp", defaults.enableBlurExp),
            enableLiquidGlass = json.optBoolean("enableLiquidGlass", defaults.enableLiquidGlass),
            useBackgroundSeedColor = json.optBoolean("useBackgroundSeedColor", defaults.useBackgroundSeedColor),
            highContrast = json.optBoolean("highContrast", defaults.highContrast),
            homeLayoutStyle = HomeLayoutStyle.fromValue(json.optString("homeLayoutStyle")),
            showHomeCardIcons = json.optBoolean("showHomeCardIcons", defaults.showHomeCardIcons),
            showNavigationBarBadge = json.optBoolean("showNavigationBarBadge", defaults.showNavigationBarBadge),
        )
    }

    private fun readLimited(input: InputStream, limit: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        copyLimited(input, out, limit)
        return out.toByteArray()
    }

    private fun writeLimited(input: InputStream, file: File, limit: Long) {
        file.outputStream().use { copyLimited(input, it, limit) }
    }

    private fun copyLimited(input: InputStream, output: java.io.OutputStream, limit: Long) {
        val buffer = ByteArray(16 * 1024)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) throw IOException("Package entry too large")
            output.write(buffer, 0, read)
        }
    }

    private fun File.isDecodableImage(): Boolean {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(absolutePath, bounds)
        return bounds.outWidth > 0 && bounds.outHeight > 0
    }
}
