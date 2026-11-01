package com.example.daily_app

import android.media.MediaPlayer
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.daily_app.ui.theme.DailyBlue
import com.example.daily_app.ui.theme.DailyBlueSoft
import com.example.daily_app.ui.theme.DailyBorder
import com.example.daily_app.ui.theme.DailySurface
import com.example.daily_app.ui.theme.DailyTextMuted
import com.example.daily_app.ui.theme.DailyTextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class FocusAmbience(val label: String, val resourceId: Int) {
    RAIN("Pluie", R.raw.focus_rain),
    FOREST("Forêt", R.raw.focus_forest),
    OCEAN("Océan", R.raw.focus_ocean)
}

private fun FocusAmbience.icon(): ImageVector = when (this) {
    FocusAmbience.RAIN -> AmbienceIcons.Rain
    FocusAmbience.FOREST -> AmbienceIcons.Forest
    FocusAmbience.OCEAN -> AmbienceIcons.Ocean
}

/**
 * Ambiance sonore.
 * - Les ambiances sont des bulles flottantes cliquables (icônes, sans noms).
 * - Choisir une bulle ne lance jamais le son.
 * - Le son joue uniquement si le haut-parleur est activé ET que le minuteur tourne :
 *   démarrer ou mettre en pause la session démarre ou met en pause le son.
 */
@Composable
fun FocusAmbienceCard(
    soundOn: Boolean,
    timerRunning: Boolean,
    onSoundOnChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentOnSoundOnChange by rememberUpdatedState(onSoundOnChange)

    var selected by remember { mutableStateOf(FocusAmbience.RAIN) }
    var volume by remember { mutableFloatStateOf(0.35f) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    val shouldPlay = soundOn && timerRunning

    LaunchedEffect(selected, shouldPlay) {
        player?.release()
        player = null
        if (shouldPlay) {
            val created = MediaPlayer.create(context, selected.resourceId)
            if (created != null) {
                created.isLooping = true
                created.setVolume(volume, volume)
                created.setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (player === mp) player = null
                    currentOnSoundOnChange(false)
                    true
                }
                created.start()
                player = created
            } else {
                currentOnSoundOnChange(false)
            }
        }
    }

    LaunchedEffect(volume) {
        player?.setVolume(volume, volume)
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
            player = null
        }
    }

    val status = when {
        !soundOn -> "Son désactivé"
        timerRunning -> "En lecture"
        else -> "Prêt · suit la session"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ambiance sonore",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = if (shouldPlay) DailyBlue else DailyTextMuted
            )
        }

        AmbienceBubbleField(
            selected = selected,
            playing = shouldPlay,
            onSelect = { selected = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = DailyIcons.VolumeDown,
                contentDescription = "Volume faible",
                tint = DailyTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = DailyBlue,
                    activeTrackColor = DailyBlue,
                    inactiveTrackColor = DailyBorder
                )
            )
            Icon(
                imageVector = DailyIcons.Volume,
                contentDescription = "Volume élevé",
                tint = DailyTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Bulles flottantes : chaque ambiance est un bouton qui dérive doucement
// ─────────────────────────────────────────────────────────────

private data class BubbleFloat(
    val fx: Float,       // position horizontale du centre (0..1 de la largeur)
    val top: Dp,         // position verticale
    val ampX: Dp,        // amplitude de dérive horizontale
    val ampY: Dp,        // amplitude de dérive verticale
    val periodMs: Int,   // durée d'un tour de dérive
    val phase: Float     // décalage pour que les bulles ne bougent pas en même temps
)

private val BubbleFloats = listOf(
    BubbleFloat(fx = 0.20f, top = 46.dp, ampX = 7.dp, ampY = 9.dp, periodMs = 5200, phase = 0.00f),
    BubbleFloat(fx = 0.50f, top = 14.dp, ampX = 6.dp, ampY = 10.dp, periodMs = 6400, phase = 0.33f),
    BubbleFloat(fx = 0.80f, top = 52.dp, ampX = 8.dp, ampY = 8.dp, periodMs = 7300, phase = 0.66f)
)

@Composable
private fun rememberAmbienceAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}

@Composable
private fun AmbienceBubbleField(
    selected: FocusAmbience,
    playing: Boolean,
    onSelect: (FocusAmbience) -> Unit
) {
    val animationsEnabled = rememberAmbienceAnimationsEnabled()
    val bubbleSize = 64.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        FocusAmbience.entries.forEachIndexed { index, ambience ->
            val spec = BubbleFloats[index]
            val isSelected = selected == ambience

            AmbienceBubble(
                ambience = ambience,
                selected = isSelected,
                playing = playing && isSelected,
                animationsEnabled = animationsEnabled,
                spec = spec,
                modifier = Modifier
                    .offset(x = maxWidth * spec.fx - bubbleSize / 2, y = spec.top)
                    .size(bubbleSize),
                onClick = { onSelect(ambience) }
            )
        }
    }
}

@Composable
private fun AmbienceBubble(
    ambience: FocusAmbience,
    selected: Boolean,
    playing: Boolean,
    animationsEnabled: Boolean,
    spec: BubbleFloat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()

    val transition = rememberInfiniteTransition(label = "bubble")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = spec.periodMs, easing = LinearEasing)
        ),
        label = "bubbleDrift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
        ),
        label = "bubblePulse"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.9f
            selected -> 1.12f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bubbleScale"
    )

    val fill by animateColorAsState(
        targetValue = if (selected) DailyBlueSoft else DailySurface,
        label = "bubbleFill"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) DailyBlue else DailyBorder,
        label = "bubbleBorder"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) DailyBlue else DailyTextMuted,
        label = "bubbleTint"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                val angle = 2f * PI.toFloat() * ((if (animationsEnabled) drift else 0f) + spec.phase)
                translationX = spec.ampX.toPx() * sin(angle)
                translationY = spec.ampY.toPx() * cos(angle)
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                // Onde qui s'étend autour de la bulle active quand le son joue
                if (playing && animationsEnabled) {
                    val radius = size.minDimension / 2f * (1f + 0.55f * pulse)
                    drawCircle(
                        color = DailyBlue.copy(alpha = 0.35f * (1f - pulse)),
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(fill)
                .border(1.dp, borderColor, CircleShape)
                .semantics { contentDescription = ambience.label }
                .selectable(
                    selected = selected,
                    interactionSource = source,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                )
                .drawBehind {
                    // Petit reflet pour donner un aspect de bulle
                    val inset = 8.dp.toPx()
                    drawArc(
                        color = DailyBlue.copy(alpha = 0.18f),
                        startAngle = 200f,
                        sweepAngle = 55f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - 2 * inset, size.height - 2 * inset),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ambience.icon(),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Icônes d'ambiance (dessinées en code, trait fin, sans dépendance)
// ─────────────────────────────────────────────────────────────

private fun buildAmbienceIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

private fun ImageVector.Builder.strokePath(block: PathBuilder.() -> Unit) {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block
    )
}

private object AmbienceIcons {

    // Nuage et gouttes
    val Rain: ImageVector by lazy {
        buildAmbienceIcon("AmbienceRain") {
            strokePath {
                moveTo(20f, 16.58f)
                arcTo(5f, 5f, 0f, false, false, 18f, 7f)
                horizontalLineToRelative(-1.26f)
                arcTo(8f, 8f, 0f, true, false, 4f, 15.25f)
            }
            strokePath { moveTo(16f, 13f); lineTo(16f, 21f) }
            strokePath { moveTo(8f, 13f); lineTo(8f, 21f) }
            strokePath { moveTo(12f, 15f); lineTo(12f, 23f) }
        }
    }

    // Sapin
    val Forest: ImageVector by lazy {
        buildAmbienceIcon("AmbienceForest") {
            strokePath {
                moveTo(12f, 3f)
                lineTo(7f, 10f)
                lineTo(9.5f, 10f)
                lineTo(5.5f, 16f)
                lineTo(18.5f, 16f)
                lineTo(14.5f, 10f)
                lineTo(17f, 10f)
                close()
            }
            strokePath { moveTo(12f, 16f); lineTo(12f, 21f) }
        }
    }

    // Vagues
    val Ocean: ImageVector by lazy {
        buildAmbienceIcon("AmbienceOcean") {
            listOf(7f, 12f, 17f).forEach { y ->
                strokePath {
                    moveTo(2f, y)
                    quadToRelative(2.5f, -3f, 5f, 0f)
                    reflectiveQuadToRelative(5f, 0f)
                    reflectiveQuadToRelative(5f, 0f)
                    reflectiveQuadToRelative(5f, 0f)
                }
            }
        }
    }
}