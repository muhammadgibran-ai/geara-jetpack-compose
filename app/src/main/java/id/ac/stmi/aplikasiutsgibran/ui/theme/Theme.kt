package id.ac.stmi.aplikasiutsgibran.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = lightColorScheme(
    primary = GearaOrange,
    secondary = GearaYellow,
    tertiary = GearaDarkBrown,
    background = GearaBackground,
    surface = GearaWhite,
    onPrimary = GearaWhite,
    onSecondary = GearaDarkBrown,
    onTertiary = GearaWhite,
    onBackground = GearaDarkBrown,
    onSurface = GearaDarkBrown
)

private val LightColorScheme = lightColorScheme(
    primary = GearaOrange,
    secondary = GearaYellow,
    tertiary = GearaDarkBrown,
    background = GearaBackground,
    surface = GearaWhite,
    onPrimary = GearaWhite,
    onSecondary = GearaDarkBrown,
    onTertiary = GearaWhite,
    onBackground = GearaDarkBrown,
    onSurface = GearaDarkBrown
)

@Composable
fun AplikasiUTSGibranTheme(
    darkTheme: Boolean = false, // Enforce Geara brand light theme to avoid dark boundary clipping
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce app branding
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
