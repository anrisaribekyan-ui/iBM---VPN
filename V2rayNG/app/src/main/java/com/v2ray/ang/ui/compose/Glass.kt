package com.v2ray.ang.ui.compose

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.GlassTransformTarget
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.hazeSource

/*
 * iBM glass surfaces (black + red, iOS 27 style frosted glass).
 *
 * Glass uses Haze's iOS 27-calibrated Glass material (refraction, diffusion, dark edge and
 * specular highlight). Those APIs are marked @ExperimentalHazeApi (warning level), so every use
 * is opted in per function and kept inside this file; the rest of the app only sees plain
 * Modifier extensions. Re-evaluate the opt-ins when Haze graduates Glass to stable.
 * On devices without RuntimeShader support Haze falls back to a tinted translucent surface.
 */

/** Marks content that glass surfaces on the same screen may refract. */
fun Modifier.glassBackdrop(state: HazeState): Modifier = hazeSource(state)

/**
 * A liquid-glass surface sampling [state]'s backdrop.
 * [tint] is laid over the refracted backdrop; keep it subtle so content behind stays alive.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
fun Modifier.glassSurface(
    state: HazeState,
    shape: RoundedCornerShape,
    tint: Color = glassTint(),
    interactionSource: InteractionSource? = null,
): Modifier {
    val style = remember(shape, tint, interactionSource != null) {
        GlassStyle.regular then GlassStyle {
            this.shape(shape)
            this.tint(tint)
            if (interactionSource != null) {
                pressed {
                    lightingIntensity(0.9f)
                    refractionMultiplier(1.08f)
                    scale(0.97f)
                }
            }
        }
    }
    val dark = LocalDarkTheme.current
    // Light theme: soft diffuse blue-tinted shadow and a faint white/light-blue edge highlight
    // instead of a hard border. Dark theme keeps Haze's own lighting only.
    val lift = if (dark || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) this else this
        .shadow(
            elevation = 14.dp,
            shape = shape,
            clip = false,
            ambientColor = GlassShadowColor,
            spotColor = GlassShadowColor,
        )
    val glass = lift.hazeGlass(
        input = HazeInput.Sources(state),
        style = style,
        interactionSource = interactionSource,
        interactionTransformTarget = GlassTransformTarget.MaterialAndContent,
    )
    return if (dark) glass else glass.border(1.dp, GlassEdgeLight, shape)
}

private val GlassShadowColor = Color(0xFF3A6FB5).copy(alpha = 0.22f)
private val GlassEdgeLight = Color.White.copy(alpha = 0.85f)

/** Default glass tint for the current theme. */
@Composable
fun glassTint(): Color =
    if (LocalDarkTheme.current) Color(0xFF0A0A0C).copy(alpha = 0.42f)
    else Color.White.copy(alpha = 0.70f)

/** Hairline that defines a glass or card edge on black (iOS-style 0.5dp separator). */
@Composable
fun Modifier.hairline(shape: RoundedCornerShape): Modifier {
    val color = if (LocalDarkTheme.current) Color.White.copy(alpha = 0.09f) else BrandColors.RedLight.copy(alpha = 0.10f)
    return this.border(0.5.dp, color, shape)
}

/**
 * Translucent content card used for list items that scroll beneath glass bars.
 * Cheap (no blur): a faint lift over the ambient background plus a hairline edge.
 */
@Composable
fun Modifier.contentCard(shape: RoundedCornerShape, selected: Boolean = false): Modifier {
    val dark = LocalDarkTheme.current
    val fill by animateColorAsState(
        targetValue = when {
            selected && dark -> BrandColors.Red.copy(alpha = 0.12f)
            // Light cards sit on an elevation shadow, so their fill must be opaque.
            selected -> BrandColors.RedLight.copy(alpha = 0.10f).compositeOver(Color.White)
            dark -> Color.White.copy(alpha = 0.055f)
            else -> Color.White
        },
        animationSpec = tween(220),
        label = "cardFill"
    )
    val edge by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            dark -> Color.White.copy(alpha = 0.08f)
            else -> BrandColors.RedLight.copy(alpha = 0.07f)
        },
        animationSpec = tween(220),
        label = "cardEdge"
    )
    return this
        .then(
            if (dark) Modifier else Modifier.shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = CardShadowColor,
                spotColor = CardShadowColor,
            )
        )
        .clip(shape)
        .background(fill)
        .border(if (selected) 1.dp else 0.5.dp, edge, shape)
}

private val CardShadowColor = Color(0xFF3A6FB5).copy(alpha = 0.14f)

/**
 * Ambient background: near-white (black in dark theme) with a soft blue radial glow. Glows brighter while connected so the
 * glass surfaces above it visibly react to the VPN state.
 */
@Composable
fun AmbientBackground(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val dark = LocalDarkTheme.current
    val intensity by animateFloatAsState(
        targetValue = if (active) 1f else 0.45f,
        animationSpec = tween(900),
        label = "ambient"
    )
    val base = if (dark) Color.Black else Color(0xFFF7F9FD)
    val glow = BrandColors.RedGlow
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = (if (dark) 0.34f else 0.24f) * intensity), Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.06f),
                        radius = w * 1.05f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = (if (dark) 0.42f else 0.26f) * intensity), Color.Transparent),
                        center = Offset(w * 0.9f, h * 0.98f),
                        radius = w * 1.15f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandColors.RedDeep.copy(alpha = (if (dark) 0.28f else 0.07f) * intensity), Color.Transparent),
                        center = Offset(w * 0.55f, h * 0.5f),
                        radius = w * 0.9f
                    )
                )
            },
        content = content
    )
}

/**
 * iOS-style switch (51×31). Pass [onCheckedChange] = null when the parent row owns the toggle
 * semantics; the switch is then purely visual.
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val dark = LocalDarkTheme.current
    val onColor = MaterialTheme.colorScheme.primary
    val offColor = if (dark) Color(0xFF39393D) else Color(0xFFE5E5EA)
    val track by animateColorAsState(
        targetValue = if (checked) onColor else offColor,
        animationSpec = tween(200),
        label = "switchTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 700f),
        label = "switchThumb"
    )
    val toggleModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange
        )
    } else Modifier
    Box(
        modifier = modifier
            .then(toggleModifier)
            .size(width = 51.dp, height = 31.dp)
            .clip(CircleShape)
            .background(if (enabled) track else track.copy(alpha = 0.4f))
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(2.dp)
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
