package com.soulhowling.solvelog_jetpackcompose.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import com.soulhowling.solvelog_jetpackcompose.R

enum class ThemeMode(val themeId: Int, @param:StringRes val labelResId: Int) {
    SYSTEM(0, R.string.theme_system),
    LIGHT(1, R.string.theme_light),
    DARK(2, R.string.theme_dark)
    ;
    
    companion object {
        fun getThemeModeBy(themeId: Int): ThemeMode {
            return entries.firstOrNull { it.themeId == themeId } ?: SYSTEM
        }
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SolveLogJetpackComposeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    var colorScheme: ColorScheme
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        colorScheme = when(themeMode) {
            ThemeMode.LIGHT -> {
                dynamicLightColorScheme(context)
            }

            ThemeMode.DARK -> {
                dynamicDarkColorScheme(context)
            }

            ThemeMode.SYSTEM -> {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
        }
    } else {
        colorScheme = when(themeMode) {
            ThemeMode.LIGHT -> LightColorScheme
            ThemeMode.DARK -> DarkColorScheme
            ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        }
    }

    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
}