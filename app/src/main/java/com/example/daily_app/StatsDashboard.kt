package com.example.daily_app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daily_app.ui.theme.DailyBackground
import com.example.daily_app.ui.theme.DailyBlue
import com.example.daily_app.ui.theme.DailyBlueOnSoft
import com.example.daily_app.ui.theme.DailyBlueSoft
import com.example.daily_app.ui.theme.DailyBorder
import com.example.daily_app.ui.theme.DailyGreen
import com.example.daily_app.ui.theme.DailyGreenOnSoft
import com.example.daily_app.ui.theme.DailyGreenSoft
import com.example.daily_app.ui.theme.DailyNeutralOnSoft
import com.example.daily_app.ui.theme.DailyNeutralSoft
import com.example.daily_app.ui.theme.DailyOrange
import com.example.daily_app.ui.theme.DailyRed
import com.example.daily_app.ui.theme.DailyRedSoft
import com.example.daily_app.ui.theme.DailySurface
import com.example.daily_app.ui.theme.DailyTextMuted
import com.example.daily_app.ui.theme.DailyTextPrimary

private val StatsPill = RoundedCornerShape(50)

private fun plural(count: Int, singular: String, pluralForm: String): String =
    if (count > 1) pluralForm else singular

@Composable
fun StatsDashboardScreen(tasks: List<Task>) {

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.completed }
    val pendingTasks = totalTasks - completedTasks

    val completionRate =
        if (totalTasks > 0) {
            completedTasks * 100 / totalTasks
        } else {
            0
        }

    val highPriorityPending = tasks.count {
        !it.completed && it.priority == Priority.HIGH
    }

    // Catégories : une seule teinte (bleu), la longueur de la barre suffit à comparer
    val categories = listOf(
        "Travail" to Category.WORK,
        "Personnel" to Category.PERSONAL,
        "Études" to Category.STUDY,
        "Développement" to Category.DEVELOPMENT,
        "Autre" to Category.OTHER
    )

    // Priorités : mêmes couleurs que les puces des tâches
    val priorities = listOf(
        Triple("Haute", Priority.HIGH, DailyRed),
        Triple("Moyenne", Priority.MEDIUM, DailyOrange),
        Triple("Faible", Priority.LOW, DailyGreen)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DailyBackground),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 28.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Column {
                Text(
                    text = "Statistiques",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DailyTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Visualise tes progrès et tes habitudes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DailyTextMuted
                )
            }
        }

        // Chiffre principal : taux de réussite
        item {
            StatsCard {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PROGRESSION GLOBALE",
                                style = MaterialTheme.typography.labelMedium,
                                color = DailyTextMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$completionRate",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = DailyTextPrimary
                                )

                                Text(
                                    text = "%",
                                    modifier = Modifier.padding(
                                        start = 3.dp,
                                        bottom = 8.dp
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DailyBlue
                                )
                            }

                            Text(
                                text = "$completedTasks ${plural(completedTasks, "tâche terminée", "tâches terminées")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DailyTextMuted
                            )
                        }

                        Box(
                            modifier = Modifier.size(92.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StatsRing(
                                progress = completionRate / 100f,
                                modifier = Modifier.size(92.dp)
                            )

                            Text(
                                text = "$completedTasks/$totalTasks",
                                style = MaterialTheme.typography.titleMedium,
                                color = DailyTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatsChip(
                            text = "$completedTasks ${plural(completedTasks, "terminée", "terminées")}",
                            backgroundColor = DailyGreenSoft,
                            textColor = DailyGreenOnSoft
                        )

                        StatsChip(
                            text = "$pendingTasks ${plural(pendingTasks, "restante", "restantes")}",
                            backgroundColor = DailyNeutralSoft,
                            textColor = DailyNeutralOnSoft
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Total",
                    value = totalTasks.toString(),
                    subtitle = "tâches créées",
                    accent = DailyBlue,
                    accentLight = DailyBlueSoft
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Terminées",
                    value = completedTasks.toString(),
                    subtitle = "tâches accomplies",
                    accent = DailyGreen,
                    accentLight = DailyGreenSoft
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "En attente",
                    value = pendingTasks.toString(),
                    subtitle = "à réaliser",
                    accent = DailyTextMuted,
                    accentLight = DailyNeutralSoft
                )

                // Le rouge n'apparaît que s'il y a vraiment une alerte
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Priorité haute",
                    value = highPriorityPending.toString(),
                    subtitle = "non terminées",
                    accent = if (highPriorityPending > 0) DailyRed else DailyTextMuted,
                    accentLight = if (highPriorityPending > 0) DailyRedSoft else DailyNeutralSoft
                )
            }
        }

        item {
            StatsSectionCard(
                title = "Répartition par catégorie",
                subtitle = "Comment tes tâches se répartissent"
            ) {
                categories.forEachIndexed { index, (label, category) ->

                    val count = tasks.count {
                        it.category == category
                    }

                    val proportion =
                        if (totalTasks > 0) {
                            count.toFloat() / totalTasks
                        } else {
                            0f
                        }

                    BreakdownRow(
                        label = label,
                        count = count,
                        total = totalTasks,
                        progress = proportion,
                        color = DailyBlue,
                        index = index
                    )
                }

                if (totalTasks == 0) {
                    EmptyStatsMessage(
                        text = "Ajoute des tâches pour découvrir leur répartition."
                    )
                }
            }
        }

        item {
            StatsSectionCard(
                title = "Niveau de priorité",
                subtitle = "Répartition de toutes tes tâches"
            ) {
                priorities.forEachIndexed { index, (label, priority, color) ->

                    val count = tasks.count {
                        it.priority == priority
                    }

                    val proportion =
                        if (totalTasks > 0) {
                            count.toFloat() / totalTasks
                        } else {
                            0f
                        }

                    BreakdownRow(
                        label = label,
                        count = count,
                        total = totalTasks,
                        progress = proportion,
                        color = color,
                        index = index
                    )
                }

                if (totalTasks == 0) {
                    EmptyStatsMessage(
                        text = "Tes niveaux de priorité apparaîtront ici."
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = DailyBlueSoft
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = when {
                            totalTasks == 0 ->
                                "Tout commence ici"

                            completionRate == 100 ->
                                "Excellent travail !"

                            completionRate >= 70 ->
                                "Tu avances très bien !"

                            completionRate >= 40 ->
                                "Continue sur ta lancée"

                            else ->
                                "Un pas après l’autre"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = DailyBlueOnSoft
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            totalTasks == 0 ->
                                "Crée ta première tâche pour commencer à suivre tes progrès."

                            completionRate == 100 ->
                                "Toutes tes tâches sont terminées. Tu peux être fier de tes efforts."

                            else ->
                                "Tu as terminé $completedTasks ${plural(completedTasks, "tâche", "tâches")} sur $totalTasks. Choisis une prochaine tâche et avance à ton rythme."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = DailyTextPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "Les statistiques reflètent toutes les tâches enregistrées.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = DailyTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Composants de l'écran Stats
// ─────────────────────────────────────────────────────────────

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DailySurface,
        border = BorderStroke(1.dp, DailyBorder),
        content = content
    )
}

@Composable
private fun StatsChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = StatsPill
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun StatsRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "statsRing"
    )

    Canvas(modifier = modifier) {
        val stroke = 8.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)

        drawArc(
            color = DailyBlueSoft,
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
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    accentLight: Color
) {
    StatsCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = accentLight,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = accent,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DailyTextMuted
            )
        }
    }
}

@Composable
private fun StatsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    StatsCard {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DailyTextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    count: Int,
    total: Int,
    progress: Float,
    color: Color,
    index: Int = 0
) {
    // Les barres se remplissent en cascade à l'ouverture de l'écran
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, delayMillis = index * 60),
        label = "breakdownProgress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = DailyTextPrimary
            )

            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                color = DailyTextPrimary
            )

            Text(
                text = " / $total",
                style = MaterialTheme.typography.bodySmall,
                color = DailyTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(StatsPill),
            color = color,
            trackColor = DailyBorder
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyStatsMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = DailyTextMuted
    )
}