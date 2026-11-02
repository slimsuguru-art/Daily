package com.example.daily_app



import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daily_app.ui.theme.DailyAppTheme
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
import com.example.daily_app.ui.theme.DailyOrangeOnSoft
import com.example.daily_app.ui.theme.DailyOrangeSoft
import com.example.daily_app.ui.theme.DailyOutline
import com.example.daily_app.ui.theme.DailyRed
import com.example.daily_app.ui.theme.DailyRedOnSoft
import com.example.daily_app.ui.theme.DailyRedSoft
import com.example.daily_app.ui.theme.DailySurface
import com.example.daily_app.ui.theme.DailyTextMuted
import com.example.daily_app.ui.theme.DailyTextPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DailyAppTheme {
                DailyAppApp()
            }
        }
    }
}

private enum class DailyTab {
    TODAY,
    FOCUS,
    STATS,
    HISTORY
}

// ─────────────────────────────────────────────────────────────
// Application : navigation et état (logique inchangée)
// ─────────────────────────────────────────────────────────────

@Composable
fun DailyAppApp(
    viewModel: DailyViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        DailyNotificationManager.createNotificationChannel(context)
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }
    val currentTimeStr = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    LaunchedEffect(tasks) {
        tasks.forEach { task ->
            if (!task.completed && task.date == todayStr && task.time == currentTimeStr) {
                DailyNotificationManager.showTaskReminderNotification(
                    context = context,
                    taskTitle = task.title,
                    timeStr = task.time
                )
            }
        }
    }

    val pendingTasksCount = tasks.count { !it.completed }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            DailyNotificationManager.showPendingTasksNotification(
                context = context,
                pendingCount = pendingTasksCount
            )
        }
    }

    val remindPendingTasks: () -> Unit = {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            DailyNotificationManager.showPendingTasksNotification(
                context = context,
                pendingCount = pendingTasksCount
            )
        }
    }
    

    var selectedTab by remember {
        mutableStateOf(DailyTab.TODAY)
    }

    var showAddSheet by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    var focusTask by remember {
        mutableStateOf<Task?>(null)
    }

    if (focusTask != null) {
        FocusScreen(
            task = focusTask!!,
            viewModel = viewModel,
            onBack = {
                focusTask = null
            }
        )

        return
    }

    Scaffold(
        containerColor = DailyBackground,
        bottomBar = {
            DailyNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == DailyTab.TODAY) {
                val fabSource = remember { MutableInteractionSource() }
                val fabScale = pressScale(fabSource, 0.92f)

                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        showAddSheet = true
                    },
                    modifier = Modifier.graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                    },
                    interactionSource = fabSource,
                    containerColor = DailyBlue,
                    contentColor = DailySurface,
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = DailyIcons.Add,
                        contentDescription = "Ajouter une tâche"
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (selectedTab) {

                DailyTab.TODAY -> {
                    TodayScreen(
                        tasks = tasks,
                        viewModel = viewModel,
                        onEditTask = {
                            editingTask = it
                            showAddSheet = true
                        },
                        onFocusTask = {
                            focusTask = it
                        },
                        onRemindPendingTasks = remindPendingTasks
                    )
                }

                DailyTab.FOCUS -> {
                    FocusTab(
                        tasks = tasks,
                        onStartFocus = {
                            focusTask = it
                        }
                    )
                }

                DailyTab.STATS -> {
                    StatsTab(
                        tasks = tasks
                    )
                }

                DailyTab.HISTORY -> {
                    HistoryTab(
                        tasks = tasks
                    )
                }
            }
        }
    }

    if (showAddSheet) {

        AddTaskSheet(
            task = editingTask,
            onDismiss = {
                showAddSheet = false
                editingTask = null
            },
            onSave = {
                    title,
                    priority,
                    category,
                    date,
                    time ->

                if (editingTask == null) {

                    viewModel.addTask(
                        title = title,
                        priority = priority,
                        category = category,
                        date = date,
                        time = time
                    )

                } else {

                    viewModel.updateTask(
                        taskId = editingTask!!.id,
                        title = title,
                        priority = priority,
                        category = category,
                        date = date,
                        time = time
                    )
                }

                showAddSheet = false
                editingTask = null
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Composants communs
// ─────────────────────────────────────────────────────────────

private val ButtonShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(50)

private data class PriorityStyle(
    val soft: Color,
    val strong: Color
)

private fun priorityStyle(priority: Priority): PriorityStyle {
    return when (priority) {
        Priority.HIGH -> PriorityStyle(DailyRedSoft, DailyRedOnSoft)
        Priority.MEDIUM -> PriorityStyle(DailyOrangeSoft, DailyOrangeOnSoft)
        Priority.LOW -> PriorityStyle(DailyGreenSoft, DailyGreenOnSoft)
    }
}

/** Effet de pression : l'élément se réduit légèrement pendant l'appui, puis revient avec un léger rebond. */
@Composable
private fun pressScale(
    source: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Float {
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )
    return scale
}

/** Carte standard : surface blanche, bordure fine, sans ombre. */
@Composable
private fun DailyCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = DailySurface,
        border = BorderStroke(1.dp, DailyBorder)
    ) {
        Column(content = content)
    }
}

/** Puce d'information (catégorie, priorité, compteur). */
@Composable
private fun DailyChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = PillShape
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

/** Cercle de validation : vide, ou vert avec une coche animée. */
@Composable
private fun DailyCheck(
    checked: Boolean,
    label: String,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    val scale = pressScale(source, 0.86f)

    val fill by animateColorAsState(
        targetValue = if (checked) DailyGreen else Color.Transparent,
        label = "checkFill"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) DailyGreen else DailyOutline,
        label = "checkBorder"
    )
    val tick by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkTick"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = label }
            .toggleable(
                value = checked,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
                role = Role.Checkbox,
                interactionSource = source,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = DailyIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        alpha = tick.coerceIn(0f, 1f)
                        scaleX = tick
                        scaleY = tick
                    }
            )
        }
    }
}

@Composable
private fun ScreenHeader(
    overline: String? = null,
    title: String,
    subtitle: String
) {
    Column {
        if (overline != null) {
            Text(
                text = overline,
                style = MaterialTheme.typography.labelMedium,
                color = DailyBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = DailyTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = DailyTextMuted
        )
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

private data class NavEntry(
    val tab: DailyTab,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun DailyNavigationBar(
    selectedTab: DailyTab,
    onTabSelected: (DailyTab) -> Unit
) {
    val entries = listOf(
        NavEntry(DailyTab.TODAY, "Aujourd’hui", DailyIcons.Today),
        NavEntry(DailyTab.FOCUS, "Focus", DailyIcons.Focus),
        NavEntry(DailyTab.STATS, "Stats", DailyIcons.Stats),
        NavEntry(DailyTab.HISTORY, "Historique", DailyIcons.History)
    )

    Column {
        HorizontalDivider(color = DailyBorder)

        NavigationBar(
            containerColor = DailySurface,
            tonalElevation = 0.dp
        ) {
            entries.forEach { entry ->
                val selected = selectedTab == entry.tab

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        onTabSelected(entry.tab)
                    },
                    icon = {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DailyBlue,
                        selectedTextColor = DailyBlue,
                        indicatorColor = DailyBlueSoft,
                        unselectedIconColor = DailyTextMuted,
                        unselectedTextColor = DailyTextMuted
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Écran Aujourd'hui
// ─────────────────────────────────────────────────────────────

@Composable
private fun TodayScreen(
    tasks: List<Task>,
    viewModel: DailyViewModel,
    onEditTask: (Task) -> Unit,
    onFocusTask: (Task) -> Unit,
    onRemindPendingTasks: () -> Unit
) {
    val completedTasks =
        tasks.count { it.completed }

    val totalTasks =
        tasks.size

    val progress =
        if (totalTasks == 0) {
            0f
        } else {
            completedTasks.toFloat() / totalTasks.toFloat()
        }

    val percentage =
        if (totalTasks == 0) {
            0
        } else {
            (progress * 100).toInt()
        }

    val remainingTasks =
        totalTasks - completedTasks

    val focusTask =
        DailyFocus.getRecommendedTask(tasks)

    // Tâches à faire d'abord, tâches terminées ensuite (l'ordre relatif est conservé)
    val orderedTasks =
        remember(tasks) {
            tasks.sortedBy { it.completed }
        }

    val dateLabel =
        remember {
            SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
                .format(Calendar.getInstance().time)
                .replaceFirstChar { it.uppercase() }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DailyBackground),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 28.dp,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            ScreenHeader(
                overline = "Bonjour · $dateLabel",
                title = "Ta journée",
                subtitle = "Avance à ton rythme, une tâche après l’autre."
            )
        }

        item {
            ProgressCard(
                completedTasks = completedTasks,
                remainingTasks = remainingTasks,
                totalTasks = totalTasks,
                percentage = percentage,
                progress = progress
            )
        }

        item {
            OutlinedButton(
                onClick = onRemindPendingTasks,
                modifier = Modifier.fillMaxWidth(),
                shape = ButtonShape,
                border = BorderStroke(1.dp, DailyBorder)
            ) {
                Text("Me rappeler mes tâches en cours ($remainingTasks)")
            }
        }

        if (focusTask != null) {

            item {
                DailyFocusCard(
                    task = focusTask,
                    onStart = {
                        onFocusTask(focusTask)
                    }
                )
            }
        }

        item {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Mes tâches",
                    style = MaterialTheme.typography.titleLarge,
                    color = DailyTextPrimary
                )

                Text(
                    text = "$remainingTasks restante${if (remainingTasks > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DailyTextMuted
                )
            }
        }

        if (tasks.isEmpty()) {

            item {
                EmptyTasksCard()
            }

        } else {

            items(
                items = orderedTasks,
                key = {
                    it.id
                }
            ) { task ->

                TaskCard(
                    task = task,
                    onToggle = {
                        viewModel.toggleTask(task.id)
                    },
                    onEdit = {
                        onEditTask(task)
                    },
                    onDelete = {
                        viewModel.deleteTask(task.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "todayProgress"
    )

    Canvas(modifier = modifier) {
        val stroke = 7.dp.toPx()
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
private fun ProgressCard(
    completedTasks: Int,
    remainingTasks: Int,
    totalTasks: Int,
    percentage: Int,
    progress: Float
) {
    DailyCard {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {

                ProgressRing(
                    progress = progress,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium,
                    color = DailyTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Progression du jour",
                    style = MaterialTheme.typography.titleMedium,
                    color = DailyTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$completedTasks tâche${if (completedTasks > 1) "s" else ""} terminée${if (completedTasks > 1) "s" else ""} sur $totalTasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DailyTextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    DailyChip(
                        text = "$completedTasks terminée${if (completedTasks > 1) "s" else ""}",
                        backgroundColor = DailyGreenSoft,
                        textColor = DailyGreenOnSoft
                    )

                    DailyChip(
                        text = "$remainingTasks restante${if (remainingTasks > 1) "s" else ""}",
                        backgroundColor = DailyNeutralSoft,
                        textColor = DailyNeutralOnSoft
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyFocusCard(
    task: Task,
    onStart: () -> Unit
) {
    val buttonSource = remember { MutableInteractionSource() }
    val buttonScale = pressScale(buttonSource)
    val style = priorityStyle(task.priority)

    DailyCard {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconBadge(
                    icon = DailyIcons.Focus,
                    backgroundColor = DailyBlueSoft,
                    iconColor = DailyBlue,
                    size = 36.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {

                    Text(
                        text = "DAILY FOCUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = DailyBlue
                    )

                    Text(
                        text = "Ta prochaine priorité",
                        style = MaterialTheme.typography.bodySmall,
                        color = DailyTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                DailyChip(
                    text = categoryLabel(task.category),
                    backgroundColor = DailyNeutralSoft,
                    textColor = DailyNeutralOnSoft
                )

                DailyChip(
                    text = priorityLabel(task.priority),
                    backgroundColor = style.soft,
                    textColor = style.strong
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seul bouton bleu plein de l'écran : l'action principale
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    },
                interactionSource = buttonSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DailyBlue,
                    contentColor = DailySurface
                ),
                shape = ButtonShape
            ) {

                Icon(
                    imageVector = DailyIcons.Play,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Commencer une session",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val style = priorityStyle(task.priority)

    val schedule = listOfNotNull(
        task.date?.let { formatHistoryDate(it) },
        task.time
    ).joinToString(" · ")

    DailyCard(
        modifier = Modifier.graphicsLayer {
            alpha = if (task.completed) 0.65f else 1f
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            DailyCheck(
                checked = task.completed,
                label = task.title,
                onToggle = onToggle
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
            ) {

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) DailyTextMuted else DailyTextPrimary,
                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    DailyChip(
                        text = categoryLabel(task.category),
                        backgroundColor = DailyNeutralSoft,
                        textColor = DailyNeutralOnSoft
                    )

                    DailyChip(
                        text = priorityLabel(task.priority),
                        backgroundColor = style.soft,
                        textColor = style.strong
                    )
                }

                if (schedule.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = schedule,
                        style = MaterialTheme.typography.bodySmall,
                        color = DailyTextMuted
                    )
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = DailyIcons.Edit,
                    contentDescription = "Modifier",
                    tint = DailyTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = DailyIcons.Delete,
                    contentDescription = "Supprimer",
                    tint = DailyRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksCard() {
    DailyCard {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            IconBadge(
                icon = DailyIcons.Today,
                backgroundColor = DailyBlueSoft,
                iconColor = DailyBlue,
                size = 56.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ta journée est libre",
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ajoute une tâche pour commencer à organiser ta journée.",
                style = MaterialTheme.typography.bodyMedium,
                color = DailyTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Onglet Focus (liste des tâches disponibles)
// ─────────────────────────────────────────────────────────────

@Composable
private fun FocusTab(
    tasks: List<Task>,
    onStartFocus: (Task) -> Unit
) {
    val activeTasks =
        tasks.filter {
            !it.completed
        }

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
            ScreenHeader(
                title = "Focus",
                subtitle = "Concentre-toi sur une tâche à la fois."
            )
        }

        item {

            DailyCard {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconBadge(
                        icon = DailyIcons.Focus,
                        backgroundColor = DailyBlueSoft,
                        iconColor = DailyBlue,
                        size = 44.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {

                        Text(
                            text = "Session Focus",
                            style = MaterialTheme.typography.titleMedium,
                            color = DailyTextPrimary
                        )

                        Text(
                            text = "25 minutes pour avancer sans distraction.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DailyTextMuted
                        )
                    }
                }
            }
        }

        item {

            Text(
                text = "Tâches disponibles",
                style = MaterialTheme.typography.titleLarge,
                color = DailyTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (activeTasks.isEmpty()) {

            item {
                EmptyTasksCard()
            }

        } else {

            items(
                items = activeTasks,
                key = {
                    it.id
                }
            ) { task ->

                DailyCard {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = DailyTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = categoryLabel(task.category),
                                style = MaterialTheme.typography.bodySmall,
                                color = DailyTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Bouton discret : le bleu plein est réservé à l'action principale
                        Button(
                            onClick = {
                                onStartFocus(task)
                            },
                            modifier = Modifier.size(44.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DailyBlueSoft,
                                contentColor = DailyBlue
                            )
                        ) {

                            Icon(
                                imageVector = DailyIcons.Play,
                                contentDescription = "Démarrer une session",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsTab(
    tasks: List<Task>
) {
    StatsDashboardScreen(
        tasks = tasks
    )
}

// ─────────────────────────────────────────────────────────────
// Historique
// ─────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(
    tasks: List<Task>
) {
    val completedTasks = tasks.filter { it.completed }
    val groupedTasks = completedTasks.groupBy { it.date ?: "Sans date" }
    val sortedDates = groupedTasks.keys.sortedWith(
        compareBy<String> { it == "Sans date" }.thenByDescending { it }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DailyBackground),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 28.dp,
            bottom = 36.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                overline = "TON PARCOURS",
                title = "Historique",
                subtitle = "Toutes les petites victoires méritent d’être vues."
            )
        }

        item {
            HistorySummaryCard(
                completedCount = completedTasks.size,
                totalCount = tasks.size
            )
        }

        if (completedTasks.isEmpty()) {
            item { EmptyHistoryCard() }
        } else {
            sortedDates.forEach { date ->
                val dateTasks = groupedTasks[date].orEmpty()
                item(key = "history_date_$date") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatHistoryDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            color = DailyTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        DailyChip(
                            text = "${dateTasks.size}",
                            backgroundColor = DailyNeutralSoft,
                            textColor = DailyNeutralOnSoft
                        )
                    }
                }
                items(
                    items = dateTasks,
                    key = { "history_${it.id}" }
                ) { task ->
                    HistoryTaskCard(task = task)
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryCard(
    completedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "historyProgress"
    )

    DailyCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TES VICTOIRES",
                        style = MaterialTheme.typography.labelMedium,
                        color = DailyTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = completedCount.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = DailyTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (completedCount == 1) "tâche terminée" else "tâches terminées",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DailyTextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                IconBadge(
                    icon = DailyIcons.Check,
                    backgroundColor = DailyGreenSoft,
                    iconColor = DailyGreen,
                    size = 48.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(PillShape),
                color = DailyGreen,
                trackColor = DailyGreenSoft
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (totalCount == 0) {
                    "Commence par terminer ta première tâche."
                } else {
                    "${(progress * 100).toInt()} % de tes tâches sont terminées. Continue comme ça !"
                },
                style = MaterialTheme.typography.bodySmall,
                color = DailyTextMuted
            )
        }
    }
}

@Composable
private fun HistoryTaskCard(
    task: Task
) {
    val style = priorityStyle(task.priority)

    DailyCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DailyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = DailyIcons.Check,
                    contentDescription = "Tâche terminée",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DailyTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DailyChip(
                        text = categoryLabel(task.category),
                        backgroundColor = DailyNeutralSoft,
                        textColor = DailyNeutralOnSoft
                    )
                    DailyChip(
                        text = priorityLabel(task.priority),
                        backgroundColor = style.soft,
                        textColor = style.strong
                    )
                }

                if (!task.time.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Heure prévue · ${task.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DailyTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    DailyCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(
                icon = DailyIcons.History,
                backgroundColor = DailyBlueSoft,
                iconColor = DailyBlue,
                size = 64.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ta première victoire t’attend",
                style = MaterialTheme.typography.titleLarge,
                color = DailyTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quand tu coches une tâche comme terminée, elle apparaîtra ici. Tu pourras retrouver tes avancées en un coup d’œil.",
                style = MaterialTheme.typography.bodyMedium,
                color = DailyTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Feuille « Nouvelle tâche / Modifier la tâche »
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskSheet(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (
        String,
        Priority,
        Category,
        String?,
        String?
    ) -> Unit
) {
    val isEditing =
        task != null

    var title by remember(task?.id) {
        mutableStateOf(task?.title ?: "")
    }

    var priority by remember(task?.id) {
        mutableStateOf(
            task?.priority ?: Priority.MEDIUM
        )
    }

    var category by remember(task?.id) {
        mutableStateOf(
            task?.category ?: Category.OTHER
        )
    }

    var selectedDate by remember(task?.id) {
        mutableStateOf(task?.date)
    }

    var selectedTime by remember(task?.id) {
        mutableStateOf(task?.time)
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    val context =
        LocalContext.current

    val categoryRows = listOf(
        listOf(Category.WORK, Category.PERSONAL),
        listOf(Category.STUDY, Category.DEVELOPMENT),
        listOf(Category.OTHER)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DailySurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                )
        ) {

            Text(
                text = if (isEditing) {
                    "Modifier la tâche"
                } else {
                    "Nouvelle tâche"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Titre")
                },
                placeholder = {
                    Text("Ex. Finaliser mon projet")
                },
                singleLine = true,
                shape = ButtonShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DailyBlue,
                    unfocusedBorderColor = DailyBorder,
                    focusedLabelColor = DailyBlue,
                    cursorColor = DailyBlue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Priorité",
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                PriorityButton(
                    text = "Faible",
                    selected = priority == Priority.LOW,
                    style = priorityStyle(Priority.LOW),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        priority = Priority.LOW
                    }
                )

                PriorityButton(
                    text = "Moyenne",
                    selected = priority == Priority.MEDIUM,
                    style = priorityStyle(Priority.MEDIUM),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        priority = Priority.MEDIUM
                    }
                )

                PriorityButton(
                    text = "Haute",
                    selected = priority == Priority.HIGH,
                    style = priorityStyle(Priority.HIGH),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        priority = Priority.HIGH
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Catégorie",
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                categoryRows.forEach { rowCategories ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        rowCategories.forEach { item ->

                            CategoryChip(
                                category = item,
                                selected = category == item,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    category = item
                                }
                            )
                        }

                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Planification",
                style = MaterialTheme.typography.titleMedium,
                color = DailyTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {

                        val calendar =
                            Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->

                                selectedDate =
                                    String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d",
                                        year,
                                        month + 1,
                                        dayOfMonth
                                    )
                            },
                            calendar.get(
                                Calendar.YEAR
                            ),
                            calendar.get(
                                Calendar.MONTH
                            ),
                            calendar.get(
                                Calendar.DAY_OF_MONTH
                            )
                        ).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = ButtonShape,
                    border = BorderStroke(1.dp, DailyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DailyTextPrimary
                    )
                ) {

                    Text(
                        text = selectedDate?.let { formatHistoryDate(it) }
                            ?: "Choisir une date",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {

                        val calendar =
                            Calendar.getInstance()

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->

                                selectedTime =
                                    String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        hour,
                                        minute
                                    )
                            },
                            calendar.get(
                                Calendar.HOUR_OF_DAY
                            ),
                            calendar.get(
                                Calendar.MINUTE
                            ),
                            true
                        ).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = ButtonShape,
                    border = BorderStroke(1.dp, DailyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DailyTextPrimary
                    )
                ) {

                    Text(
                        text = selectedTime
                            ?: "Choisir une heure",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {

                    if (title.trim().isNotEmpty()) {

                        onSave(
                            title.trim(),
                            priority,
                            category,
                            selectedDate,
                            selectedTime
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = title.trim().isNotEmpty(),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DailyBlue,
                    contentColor = DailySurface,
                    disabledContainerColor = DailyBorder,
                    disabledContentColor = DailyTextMuted
                )
            ) {

                Text(
                    text = if (isEditing) {
                        "Enregistrer les modifications"
                    } else {
                        "Ajouter la tâche"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun PriorityButton(
    text: String,
    selected: Boolean,
    style: PriorityStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = ButtonShape,
        color = if (selected) style.soft else DailySurface,
        border = BorderStroke(1.dp, if (selected) style.strong else DailyBorder)
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) style.strong else DailyTextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = ButtonShape,
        color = if (selected) DailyBlueSoft else DailySurface,
        border = BorderStroke(1.dp, if (selected) DailyBlue else DailyBorder)
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = categoryLabel(category),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) DailyBlueOnSoft else DailyTextPrimary,
                maxLines = 1
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Libellés et formats (inchangés)
// ─────────────────────────────────────────────────────────────

private fun priorityLabel(
    priority: Priority
): String {
    return when (priority) {
        Priority.LOW -> "Faible"
        Priority.MEDIUM -> "Moyenne"
        Priority.HIGH -> "Haute"
    }
}

private fun categoryLabel(
    category: Category
): String {
    return when (category) {
        Category.WORK -> "Travail"
        Category.PERSONAL -> "Personnel"
        Category.STUDY -> "Études"
        Category.DEVELOPMENT -> "Développement"
        Category.OTHER -> "Autre"
    }
}

private fun formatHistoryDate(
    date: String
): String {
    if (date == "Sans date") {
        return date
    }

    return try {

        val input =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val output =
            SimpleDateFormat(
                "EEEE d MMMM",
                Locale.getDefault()
            )

        val parsedDate =
            input.parse(date)

        if (parsedDate != null) {
            output.format(parsedDate)
                .replaceFirstChar {
                    it.uppercase()
                }
        } else {
            date
        }

    } catch (_: Exception) {
        date
    }
}
