package com.example.daily_app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DailyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),   // puces
    medium = RoundedCornerShape(16.dp),  // cartes
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp) // feuilles modales
)