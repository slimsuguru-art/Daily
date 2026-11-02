package com.example.daily_app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Gestion centralisée des notifications de Daily. */
object DailyNotificationManager {

    private const val CHANNEL_ID = "daily_test_notifications"
    private const val TEST_NOTIFICATION_ID = 1001
    private const val PENDING_TASKS_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications Daily",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels et notifications de l'application Daily"
            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** Notification de test conservée pour les tests techniques. */
    fun showTestNotification(context: Context): Boolean {
        if (!hasNotificationPermission(context)) return false

        createNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily fonctionne !")
            .setContentText("Les notifications sont activées.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(TEST_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            return false
        }
        return true
    }

    /** Affiche le nombre de tâches restant à terminer. */
    fun showPendingTasksNotification(
        context: Context,
        pendingCount: Int
    ): Boolean {
        if (!hasNotificationPermission(context)) return false

        createNotificationChannel(context)
        val count = pendingCount.coerceAtLeast(0)
        val title: String
        val message: String

        if (count > 0) {
            title = "Rappel Daily 🎯"
            message = if (count == 1) {
                "Il te reste 1 tâche à terminer aujourd'hui. Tu peux y arriver !"
            } else {
                "Il te reste $count tâches à terminer aujourd'hui. Tu peux y arriver !"
            }
        } else {
            title = "Bravo ! 🎉"
            message = "Toutes tes tâches sont terminées pour aujourd'hui."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(PENDING_TASKS_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            return false
        }
        return true
    }

    /** Rappel automatique lorsqu'une tâche programmée arrive à échéance. */
    fun showTaskReminderNotification(
        context: Context,
        taskTitle: String,
        timeStr: String?
    ): Boolean {
        if (!hasNotificationPermission(context)) return false

        createNotificationChannel(context)
        val message = if (!timeStr.isNullOrBlank()) {
            "C'est l'heure de ta tâche programmée à $timeStr : \"$taskTitle\""
        } else {
            "Rappel pour ta tâche : \"$taskTitle\""
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rappel de tâche ⏰")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(PENDING_TASKS_NOTIFICATION_ID + taskTitle.hashCode(), notification)
        } catch (_: SecurityException) {
            return false
        }
        return true
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }
}
