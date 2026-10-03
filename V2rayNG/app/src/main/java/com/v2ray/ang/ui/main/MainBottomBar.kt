package com.v2ray.ang.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.BrandColors
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.glassSurface
import dev.chrisbanes.haze.HazeState

private val PanelShape = RoundedCornerShape(32.dp)

/**
 * Floating liquid-glass control panel: connection status, the connect button and the quick
 * settings (bypass .ru, per-app VPN).
 */
@Composable
fun MainBottomBar(
    hazeState: HazeState,
    isRunning: Boolean,
    statusTitle: String,
    statusSubtitle: String,
    statusHint: String?,
    ruBypassEnabled: Boolean,
    perAppProxyEnabled: Boolean,
    onAction: (MainAction) -> Unit,
    onChooseApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
            .glassSurface(hazeState, PanelShape)
            .padding(top = 6.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 14.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBlock(
                isRunning = isRunning,
                title = statusTitle,
                subtitle = statusSubtitle,
                hint = statusHint,
                onTest = { onAction(MainAction.TestCurrentServer) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            ConnectButton(
                isRunning = isRunning,
                onClick = { onAction(MainAction.ToggleService) }
            )
        }
        AppDivider(modifier = Modifier.padding(horizontal = 20.dp))
        MainQuickSettings(
            ruBypassEnabled = ruBypassEnabled,
            perAppProxyEnabled = perAppProxyEnabled,
            onAction = onAction,
            onChooseApps = onChooseApps
        )
    }
}

@Composable
private fun StatusBlock(
    isRunning: Boolean,
    title: String,
    subtitle: String,
    hint: String?,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = if (isRunning) BrandColors.Green else MaterialTheme.colorScheme.outline,
        animationSpec = tween(400),
        label = "statusDot"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                onClickLabel = stringResource(R.string.connection_test_pending),
                onClick = onTest
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(9.dp)
                    .drawBehind {
                        if (isRunning) {
                            drawCircle(dotColor.copy(alpha = 0.35f), radius = size.minDimension)
                        }
                    }
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "statusTitle"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Large round connect button: quiet glass when off, glowing red when the tunnel is up. */
@Composable
private fun ConnectButton(
    isRunning: Boolean,
    onClick: () -> Unit
) {
    val dark = LocalDarkTheme.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "connectScale"
    )
    val activeAmount by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "connectActive"
    )
    val breathing = rememberInfiniteTransition(label = "connectBreathing")
    val glowPulse by breathing.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "connectGlow"
    )
    val idleFill = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val idleIcon = MaterialTheme.colorScheme.onSurface
    val iconTint by animateColorAsState(
        targetValue = if (isRunning) Color.White else idleIcon,
        animationSpec = tween(300),
        label = "connectIcon"
    )
    val label = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start)

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .drawBehind {
                if (activeAmount > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrandColors.RedGlow.copy(alpha = 0.55f * activeAmount * glowPulse),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.95f
                        ),
                        radius = size.minDimension * 0.95f
                    )
                }
            }
            .clip(CircleShape)
            .background(idleFill)
            .drawBehind {
                if (activeAmount > 0f) {
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(BrandColors.Red, BrandColors.RedDeep),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                        ),
                        alpha = activeAmount
                    )
                }
            }
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = if (dark) 0.18f else 0.6f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.performHapticFeedback(
                    if (isRunning) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn
                )
                onClick()
            }
            .semantics {
                role = Role.Button
                contentDescription = label
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_power_24dp),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(30.dp)
        )
    }
}
