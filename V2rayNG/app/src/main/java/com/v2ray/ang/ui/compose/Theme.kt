package com.v2ray.ang.ui.compose

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ---------------------------------------------------------------------------------------------
// YOUdkinVPN design tokens: black + red, Apple-style neutrals.
// ---------------------------------------------------------------------------------------------

/** Brand reds. */
object BrandColors {
    val Red = Color(0xFFFF3347)          // primary accent (dark)
    val RedDeep = Color(0xFFD90A24)      // pressed / gradient end
    val RedLight = Color(0xFFE5172C)     // primary accent on light backgrounds
    val RedGlow = Color(0xFFFF1F3D)      // ambient glow
    val Green = Color(0xFF32D74B)        // good latency
    val Amber = Color(0xFFFFB340)        // medium latency
}

private val LightColor = lightColorScheme(
    primary = BrandColors.RedLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3E5),
    onPrimaryContainer = Color(0xFF5C0010),
    secondary = BrandColors.RedLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3E5),
    onSecondaryContainer = Color(0xFF5C0010),
    tertiary = Color(0xFF1C1C1E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5E5EA),
    onTertiaryContainer = Color(0xFF1C1C1E),
    error = Color(0xFFD70015),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFF2F2F7),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF6C6C70),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE5E5EA),
    inverseSurface = Color(0xFF1C1C1E),
    inverseOnSurface = Color(0xFFF2F2F7),
    inversePrimary = BrandColors.Red,
    scrim = Color.Black,
    surfaceTint = Color.Transparent,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE5E5EA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF9F9FB),
    surfaceContainerHighest = Color(0xFFE9E9EE),
)

private val DarkColor = darkColorScheme(
    primary = BrandColors.Red,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D0710),
    onPrimaryContainer = Color(0xFFFFD9DC),
    secondary = BrandColors.Red,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D0710),
    onSecondaryContainer = Color(0xFFFFD9DC),
    tertiary = Color(0xFFF5F5F7),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF2C2C2E),
    onTertiaryContainer = Color(0xFFF5F5F7),
    error = Color(0xFFFF6961),
    errorContainer = Color(0xFF5C0A0A),
    onError = Color.Black,
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color(0xFFF5F5F7),
    surface = Color.Black,
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF48484A),
    outlineVariant = Color(0xFF2C2C2E),
    inverseSurface = Color(0xFFF5F5F7),
    inverseOnSurface = Color.Black,
    inversePrimary = BrandColors.RedLight,
    scrim = Color.Black,
    surfaceTint = Color.Transparent,
    surfaceBright = Color(0xFF2C2C2E),
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0E0E10),
    surfaceContainer = Color(0xFF141416),
    surfaceContainerHigh = Color(0xFF1C1C1E),
    surfaceContainerHighest = Color(0xFF2C2C2E),
)

// Semantic Colors
val colorPing = BrandColors.Green
val colorPingRed = Color(0xFFFF453A)
val colorConfigType = BrandColors.Red
val colorFabActive = BrandColors.Red
val colorFabInactiveLight = Color(0xFFC7C7CC)
val colorFabInactiveDark = Color(0xFF2C2C2E)
val dividerColorLight = Color(0x1F3C3C43) // iOS separator, light
val dividerColorDark = Color(0x29FFFFFF)  // hairline on black

// Toast Colors 90%
val toastNormalBgLight = Color(0xE61C1C1E)
val toastNormalBgDark = Color(0xE62C2C2E)
val toastSuccessBg = Color(0xE6248A3D)
val toastErrorBg = Color(0xE6D70015)
val toastInfoBg = Color(0xE62C2C2E)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

// ---------------------------------------------------------------------------------------------
// Typography: Inter, with Apple-like optical tracking (tighter as size grows).
// ---------------------------------------------------------------------------------------------

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun inter(size: Int, line: Int, weight: FontWeight, tracking: TextUnit): TextStyle = TextStyle(
    fontFamily = InterFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking,
)

val AppTypography = Typography(
    displayLarge = inter(52, 58, FontWeight.Bold, (-1.2).sp),
    displayMedium = inter(42, 48, FontWeight.Bold, (-0.9).sp),
    displaySmall = inter(34, 40, FontWeight.Bold, (-0.7).sp),
    headlineLarge = inter(30, 36, FontWeight.Bold, (-0.6).sp),
    headlineMedium = inter(26, 32, FontWeight.SemiBold, (-0.5).sp),
    headlineSmall = inter(22, 28, FontWeight.SemiBold, (-0.4).sp),
    titleLarge = inter(20, 26, FontWeight.SemiBold, (-0.35).sp),
    titleMedium = inter(17, 22, FontWeight.SemiBold, (-0.25).sp),
    titleSmall = inter(15, 20, FontWeight.SemiBold, (-0.15).sp),
    bodyLarge = inter(16, 22, FontWeight.Normal, (-0.2).sp),
    bodyMedium = inter(14, 20, FontWeight.Normal, (-0.1).sp),
    bodySmall = inter(12, 16, FontWeight.Normal, 0.sp),
    labelLarge = inter(15, 20, FontWeight.SemiBold, (-0.15).sp),
    labelMedium = inter(13, 18, FontWeight.Medium, (-0.05).sp),
    labelSmall = inter(11, 14, FontWeight.Medium, 0.1.sp),
)

/** Continuous, generous corners (iOS / One UI feel). */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

// ---------------------------------------------------------------------------------------------

/** YOUdkinVPN branding: dark black-red theme by default, system dynamic colors off. */
const val DEFAULT_UI_MODE_NIGHT = "2"
const val DEFAULT_DYNAMIC_COLOR = false

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, DEFAULT_UI_MODE_NIGHT) ?: DEFAULT_UI_MODE_NIGHT
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, DEFAULT_DYNAMIC_COLOR)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, DEFAULT_UI_MODE_NIGHT) ?: DEFAULT_UI_MODE_NIGHT
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, DEFAULT_DYNAMIC_COLOR)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // The app can force dark mode while the system is light. Components that read the night
    // bit from Configuration (e.g. glass materials picking their tone) must see the app's choice.
    val baseConfiguration = LocalConfiguration.current
    val themedConfiguration = remember(baseConfiguration, darkTheme) {
        Configuration(baseConfiguration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController,
        LocalConfiguration provides themedConfiguration
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
