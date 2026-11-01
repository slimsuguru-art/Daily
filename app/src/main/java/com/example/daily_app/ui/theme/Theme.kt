package com.example.daily_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DailyLightColorScheme = lightColorScheme(
    primary = DailyBlue,
    onPrimary = DailySurface,
    primaryContainer = DailyBlueSoft,
    onPrimaryContainer = DailyBlueOnSoft,

    secondary = DailyBlue,
    onSecondary = DailySurface,
    secondaryContainer = DailyBlueSoft,
    onSecondaryContainer = DailyBlueOnSoft,

    tertiary = DailyGreen,
    onTertiary = DailySurface,
    tertiaryContainer = DailyGreenSoft,
    onTertiaryContainer = DailyGreenOnSoft,

    error = DailyRed,
    onError = DailySurface,
    errorContainer = DailyRedSoft,
    onErrorContainer = DailyRedOnSoft,

    background = DailyBackground,
    onBackground = DailyTextPrimary,
    surface = DailySurface,
    onSurface = DailyTextPrimary,
    surfaceVariant = DailyNeutralSoft,
    onSurfaceVariant = DailyTextMuted,
    surfaceTint = DailySurface,   // évite la teinte colorée sur les cartes élevées

    outline = DailyOutline,
    outlineVariant = DailyBorder
)

// Les paramètres darkTheme et dynamicColor sont conservés pour ne pas casser
// les appels existants (ex. MainActivity), mais ils sont ignorés pour la V1 :
// l'identité Daily est volontairement fixe et claire. Le mode sombre viendra plus tard.
@Suppress("UNUSED_PARAMETER")
@Composable
fun DailyAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DailyLightColorScheme,
        typography = Typography,
        shapes = DailyShapes,
        content = content
    )
}