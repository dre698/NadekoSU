package com.nadekosu.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * "Liquid Glass" effect, inspired by iOS 26's glass material: the content behind this
 * composable gets a subtle lens-like refraction near the edges (via an AGSL RuntimeShader,
 * API 33+), topped with a specular highlight rim that reads like light catching curved glass.
 *
 * This is meant to sit ON TOP of an existing blur (e.g. [blurEffect] / [blurSource]) - it does
 * not blur by itself, it only distorts + adds the highlight, the same way a pane of glass
 * sitting on top of a blurred background would.
 *
 * - API < 31: no-op, returns [this] unchanged.
 * - API 31/32: highlight rim only (no RuntimeShader support yet).
 * - API 33+: full effect (refraction + highlight rim).
 */
fun Modifier.liquidGlassEffect(
    cornerRadius: Dp = 24.dp,
    edgeWidth: Float = 28f,
    refractionStrength: Float = 12f,
    highlightAlpha: Float = 0.55f,
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return this

    val refracted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
            val shader = RuntimeShader(LIQUID_GLASS_AGSL).apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("edgeWidth", edgeWidth)
                setFloatUniform("refractionStrength", refractionStrength)
            }
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()
        }
    } else {
        this
    }

    return refracted.then(
        Modifier.drawWithCache {
            val strokeWidth = 1.5.dp.toPx()
            val radiusPx = cornerRadius.toPx()
            val highlight = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = highlightAlpha),
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = highlightAlpha * 0.35f),
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            )
            onDrawWithContent {
                drawContent()
                drawRoundRect(
                    brush = highlight,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    )
}

/**
 * Refracts [content] toward the shape's center near its edges, with distance-based falloff so
 * the middle of the pill stays undistorted (like real glass, which only bends light near the
 * rim).
 */
private const val LIQUID_GLASS_AGSL = """
uniform shader content;
uniform float2 size;
uniform float edgeWidth;
uniform float refractionStrength;

half4 main(float2 coord) {
    float distLeft = coord.x;
    float distRight = size.x - coord.x;
    float distTop = coord.y;
    float distBottom = size.y - coord.y;
    float distEdge = min(min(distLeft, distRight), min(distTop, distBottom));

    float2 offset = float2(0.0, 0.0);
    if (distEdge < edgeWidth && edgeWidth > 0.0) {
        float2 center = size * 0.5;
        float2 toCenter = normalize(coord - center);
        float falloff = 1.0 - (distEdge / edgeWidth);
        falloff = falloff * falloff;
        offset = -toCenter * falloff * refractionStrength;
    }

    return content.eval(coord + offset);
}
"""
