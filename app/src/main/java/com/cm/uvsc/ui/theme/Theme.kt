package com.cm.uvsc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = USCVColor.Neon01,
    onPrimary = USCVColor.White,
    primaryContainer = USCVColor.White,
    onPrimaryContainer = USCVColor.Graphite,
    inversePrimary = USCVColor.Neon01,
    secondary = USCVColor.Blue04,
    onSecondary = USCVColor.White,
    secondaryContainer = USCVColor.Blue01,
    onSecondaryContainer = USCVColor.LightBlack,
    surfaceContainerLow = USCVColor.Blue01,
    tertiary = USCVColor.Yellow01,
    onTertiary = USCVColor.Black,
    tertiaryContainer = USCVColor.Yellow03A40,
    onTertiaryContainer = USCVColor.Yellow04,
    error = USCVColor.Red03,
    onError = USCVColor.White,
    errorContainer = USCVColor.Red01,
    onErrorContainer = USCVColor.Red06,
    surface = USCVColor.White,
    onSurface = USCVColor.Black,
    onSurfaceVariant = USCVColor.DarkGray,
    surfaceVariant = USCVColor.Graphite,
    surfaceDim = USCVColor.PaleGray,
    surfaceContainerHigh = USCVColor.LightGray,
    inverseSurface = USCVColor.Yellow05,
    inverseOnSurface = USCVColor.White,
    outline = USCVColor.Gainsboro,
    outlineVariant = USCVColor.DarkGray,
    scrim = USCVColor.Black,
    surfaceContainerLowest = USCVColor.PaleGray,
)

@Composable
fun UVSCTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> LightColorScheme
        else -> LightColorScheme
    }

    if (!LocalInspectionMode.current) {
        val view = LocalView.current
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                !darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}