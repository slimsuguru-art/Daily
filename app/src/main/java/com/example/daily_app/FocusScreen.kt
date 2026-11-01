package com.example.daily_app

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daily_app.ui.theme.DailyBackground
import com.example.daily_app.ui.theme.DailyBlue
import com.example.daily_app.ui.theme.DailyBlueSoft
import com.example.daily_app.ui.theme.DailyBorder
import com.example.daily_app.ui.theme.DailyGreen
import com.example.daily_app.ui.theme.DailyGreenOnSoft
import com.example.daily_app.ui.theme.DailyGreenSoft
import com.example.daily_app.ui.theme.DailyNeutralOnSoft
import com.example.daily_app.ui.theme.DailyNeutralSoft
import com.example.daily_app.ui.theme.DailyOrangeOnSoft
import com.example.daily_app.ui.theme.DailyOrangeSoft
import com.example.daily_app.ui.theme.DailyRedOnSoft
import com.example.daily_app.ui.theme.DailyRedSoft
import com.example.daily_app.ui.theme.DailySurface
import com.example.daily_app.ui.theme.DailyTextMuted
import com.example.daily_app.ui.theme.DailyTextPrimary
import kotlin.math.cos
import kotlin.math.sin

private val FocusButtonShape = RoundedCornerShape(14.dp)

@Composable
fun FocusScreen(
    task: Task,
    viewModel: DailyViewModel,
    onBack: () -> Unit
) {
    val timer: FocusTimer = viewModel()
    val remainingSeconds by timer.remainingSeconds.collectAsState()
    val isRunning by timer.isRunning.collectAsState()
    val isFinished by timer.isFinished.collectAsState()
    val totalSeconds by timer.totalDurationSeconds.collectAsState()

    var customMinutes by remember { mutableStateOf("") }
    var soundOn by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val animationsEnabled = rememberAnimationsEnabled()

    val progress = if (totalSeconds > 0) {
        1f - remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f
    val formattedTime = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    val stateLabel = when {
        isFinished -> "Bravo !"
        isRunning -> "Concentration en cours"
        remainingSeconds < totalSeconds -> "Session en pause"
        else -> "Prêt · ${totalSeconds / 60} min"
    }

    // Respiration douce de l'anneau pendant la session
    val breathe = if (animationsEnabled) {
        rememberInfiniteTransition(label = "breathe").animateFloat(
            initialValue = 1f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breatheScale"
        )
    } else null
    val breatheAmount by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "breatheAmount"
    )

    val playSource = remember { MutableInteractionSource() }
    val playScale = focusPressScale(playSource)
    val morph by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playPauseMorph"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DailyBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    if (isRunning) timer.pause()
                    onBack()
                },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = DailySurface,
                border = BorderStroke(1.dp, DailyBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = DailyIcons.Back,
                        contentDescription = "Retour",
                        tint = DailyTextPrimary
                    )
                }
            }

            Text(
                text = "Focus",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        // Tâche en cours
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = DailySurface,
            border = BorderStroke(1.dp, DailyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Tâche en cours",
                    style = MaterialTheme.typography.labelMedium,
                    color = DailyTextMuted
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DailyTextPrimary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = focusPriorityColors(task.priority)
                    FocusChip(categoryLabel(task.category), DailyNeutralSoft, DailyNeutralOnSoft)
                    FocusChip(priorityLabel(task.priority), colors.first, colors.second)
                }
            }
        }

        // Minuteur circulaire
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    val s = 1f + ((breathe?.value ?: 1f) - 1f) * breatheAmount
                    scaleX = s
                    scaleY = s
                },
            contentAlignment = Alignment.Center
        ) {
            FocusProgressCircle(progress = progress)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isFinished) "Terminé" else formattedTime,
                    style = if (isFinished) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.displayLarge
                    },
                    color = DailyTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DailyTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Contrôles : réinitialiser · démarrer/pause · son
        AnimatedVisibility(visible = !isFinished) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { timer.reset() },
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = DailySurface,
                    border = BorderStroke(1.dp, DailyBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = DailyIcons.Reset,
                            contentDescription = "Réinitialiser",
                            tint = DailyTextPrimary
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isRunning) timer.pause() else timer.start()
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = playScale
                            scaleY = playScale
                        },
                    shape = CircleShape,
                    color = DailyBlue,
                    interactionSource = playSource
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Le lecteur se transforme en pause (et inversement) avec une courte rotation
                        Icon(
                            imageVector = DailyIcons.Play,
                            contentDescription = if (isRunning) null else "Démarrer la session",
                            tint = DailySurface,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer {
                                    alpha = 1f - morph.coerceIn(0f, 1f)
                                    rotationZ = 90f * morph
                                    scaleX = 1f - 0.5f * morph
                                    scaleY = 1f - 0.5f * morph
                                }
                        )
                        Icon(
                            imageVector = DailyIcons.Pause,
                            contentDescription = if (isRunning) "Mettre en pause" else null,
                            tint = DailySurface,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer {
                                    alpha = morph.coerceIn(0f, 1f)
                                    rotationZ = -90f * (1f - morph)
                                    scaleX = 0.5f + 0.5f * morph
                                    scaleY = 0.5f + 0.5f * morph
                                }
                        )
                    }
                }

                val soundColor by animateColorAsState(
                    targetValue = if (soundOn) DailyBlueSoft else DailySurface,
                    label = "soundButtonColor"
                )
                Surface(
                    onClick = { soundOn = !soundOn },
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = soundColor,
                    border = BorderStroke(1.dp, if (soundOn) DailyBlue else DailyBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = DailyIcons.Volume,
                            contentDescription = if (soundOn) "Désactiver le son" else "Activer le son",
                            tint = if (soundOn) DailyBlue else DailyTextPrimary
                        )
                    }
                }
            }
        }

        // Fin de session : la carte de félicitations remonte du bas
        AnimatedVisibility(
            visible = isFinished,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = DailySurface,
                    border = BorderStroke(1.dp, DailyBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = DailyGreenSoft
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = DailyIcons.Check,
                                    contentDescription = null,
                                    tint = DailyGreen
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Session terminée",
                                style = MaterialTheme.typography.titleMedium,
                                color = DailyTextPrimary
                            )
                            Text(
                                text = "${totalSeconds / 60} min de concentration. Bravo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DailyTextMuted
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.toggleTask(task.id)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = FocusButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DailyGreen,
                        contentColor = DailySurface
                    )
                ) {
                    Icon(DailyIcons.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Marquer comme terminée", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = { timer.reset() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = FocusButtonShape,
                    border = BorderStroke(1.dp, DailyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DailyTextPrimary)
                ) {
                    Icon(DailyIcons.Reset, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Recommencer", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Durées prédéfinies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 15, 25, 50).forEach { minutes ->
                val selected = totalSeconds == minutes * 60
                val chipColor by animateColorAsState(
                    targetValue = if (selected) DailyBlue else DailySurface,
                    label = "presetColor"
                )
                Surface(
                    onClick = { timer.setDurationMinutes(minutes) },
                    enabled = !isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .graphicsLayer { alpha = if (isRunning && !selected) 0.5f else 1f },
                    shape = RoundedCornerShape(19.dp),
                    color = chipColor,
                    border = BorderStroke(1.dp, if (selected) DailyBlue else DailyBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$minutes min",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) DailySurface else DailyNeutralOnSoft,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Ambiance sonore : bulles flottantes cliquables, volume, synchronisée avec le minuteur
        FocusAmbienceCard(
            soundOn = soundOn,
            timerRunning = isRunning,
            onSoundOnChange = { soundOn = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Durée personnalisée (fonction conservée)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = DailySurface,
            border = BorderStroke(1.dp, DailyBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Durée personnalisée",
                    style = MaterialTheme.typography.titleMedium,
                    color = DailyTextPrimary
                )
                Text(
                    text = "Définis une durée entre 1 et 180 minutes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DailyTextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it.filter(Char::isDigit).take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Minutes") },
                        singleLine = true,
                        enabled = !isRunning,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = FocusButtonShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DailyBlue,
                            unfocusedBorderColor = DailyBorder,
                            focusedLabelColor = DailyBlue,
                            cursorColor = DailyBlue
                        )
                    )
                    Button(
                        onClick = {
                            val minutes = customMinutes.toIntOrNull()
                            if (minutes != null && minutes in 1..180) {
                                timer.setDurationMinutes(minutes)
                                customMinutes = ""
                            }
                        },
                        enabled = !isRunning && (customMinutes.toIntOrNull() in 1..180),
                        modifier = Modifier.height(48.dp),
                        shape = FocusButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DailyBlue,
                            contentColor = DailySurface,
                            disabledContainerColor = DailyBorder,
                            disabledContentColor = DailyTextMuted
                        )
                    ) {
                        Text("Appliquer", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Composants du Focus
// ─────────────────────────────────────────────────────────────

@Composable
private fun rememberAnimationsEnabled(): Boolean {
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
private fun focusPressScale(
    source: MutableInteractionSource,
    pressedScale: Float = 0.94f
): Float {
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "focusPressScale"
    )
    return scale
}

@Composable
private fun FocusProgressCircle(progress: Float) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "focusProgress"
    )

    Canvas(modifier = Modifier.size(240.dp)) {
        val stroke = 12.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)

        // Partie restante : gris clair bien contrasté avec le bleu
        drawArc(
            color = DailyBorder,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )

        if (animated > 0f) {
            drawArc(
                color = DailyBlue,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Point lumineux au bout de l'arc
        if (animated > 0.001f && animated < 0.999f) {
            val ringRadius = (size.width - stroke) / 2f
            val angle = Math.toRadians((-90f + 360f * animated).toDouble())
            val head = Offset(
                x = center.x + ringRadius * cos(angle).toFloat(),
                y = center.y + ringRadius * sin(angle).toFloat()
            )
            drawCircle(color = Color.White, radius = stroke * 0.7f, center = head)
            drawCircle(color = DailyBlue, radius = stroke * 0.42f, center = head)
        }
    }
}

@Composable
private fun FocusChip(text: String, backgroundColor: Color, textColor: Color) {
    Surface(color = backgroundColor, shape = RoundedCornerShape(50)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private fun focusPriorityColors(priority: Priority): Pair<Color, Color> = when (priority) {
    Priority.HIGH -> DailyRedSoft to DailyRedOnSoft
    Priority.MEDIUM -> DailyOrangeSoft to DailyOrangeOnSoft
    Priority.LOW -> DailyGreenSoft to DailyGreenOnSoft
}

private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.LOW -> "Faible"
    Priority.MEDIUM -> "Moyenne"
    Priority.HIGH -> "Haute"
}

private fun categoryLabel(category: Category): String = when (category) {
    Category.WORK -> "Travail"
    Category.PERSONAL -> "Personnel"
    Category.STUDY -> "Études"
    Category.DEVELOPMENT -> "Développement"
    Category.OTHER -> "Autre"
}