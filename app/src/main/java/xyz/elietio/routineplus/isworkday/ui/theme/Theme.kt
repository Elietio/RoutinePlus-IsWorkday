package xyz.elietio.routineplus.isworkday.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class HolidayColorScheme(
    val holidayBg: Color,
    val holidayText: Color,
    val workdayBg: Color,
    val workdayText: Color
)

val LocalHolidayColorScheme = staticCompositionLocalOf {
    HolidayColorScheme(
        holidayBg = Color.Unspecified,
        holidayText = Color.Unspecified,
        workdayBg = Color.Unspecified,
        workdayText = Color.Unspecified
    )
}

private val LightColorScheme = lightColorScheme(
    primary = md_light_primary,
    onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer,
    onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary,
    onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer,
    onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary,
    onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer,
    onTertiaryContainer = md_light_onTertiaryContainer,
    background = md_light_background,
    onBackground = md_light_onBackground,
    surface = md_light_surface,
    onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant,
    onSurfaceVariant = md_light_onSurfaceVariant,
    surfaceContainer = md_light_surfaceContainer,
    surfaceContainerHigh = md_light_surfaceContainerHigh,
    outline = md_light_outline,
    error = md_light_error
)

private val DarkColorScheme = darkColorScheme(
    primary = md_dark_primary,
    onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer,
    onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary,
    onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer,
    onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary,
    onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer,
    onTertiaryContainer = md_dark_onTertiaryContainer,
    background = md_dark_background,
    onBackground = md_dark_onBackground,
    surface = md_dark_surface,
    onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant,
    onSurfaceVariant = md_dark_onSurfaceVariant,
    surfaceContainer = md_dark_surfaceContainer,
    surfaceContainerHigh = md_dark_surfaceContainerHigh,
    outline = md_dark_outline,
    error = md_dark_error
)

@Composable
fun RoutinePlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val holidayColorScheme = if (darkTheme) {
        HolidayColorScheme(
            holidayBg = holidayRedDark,
            holidayText = holidayRedTextDark,
            workdayBg = workdayGreenDark,
            workdayText = workdayGreenTextDark
        )
    } else {
        HolidayColorScheme(
            holidayBg = holidayRedLight,
            holidayText = holidayRed,
            workdayBg = workdayGreenLight,
            workdayText = workdayGreen
        )
    }

    CompositionLocalProvider(
        LocalHolidayColorScheme provides holidayColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
