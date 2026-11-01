package com.example.daily_app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyFocus {

    /**
     * Trouve la tâche que Daily recommande de faire maintenant.
     */
    fun getRecommendedTask(
        tasks: List<Task>
    ): Task? {

        // On ignore les tâches déjà terminées.
        val activeTasks =
            tasks.filter {
                !it.completed
            }

        // Aucune tâche disponible.
        if (activeTasks.isEmpty()) {
            return null
        }

        val today =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())

        val currentTime =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())

        return activeTasks.maxByOrNull { task ->

            var score = 0

            // --------------------------------
            // 1. PRIORITÉ
            // --------------------------------

            score += when (task.priority) {

                Priority.HIGH -> 3

                Priority.MEDIUM -> 2

                Priority.LOW -> 1
            }

            // --------------------------------
            // 2. DATE
            // --------------------------------

            if (task.date == today) {

                score += 2
            }

            // --------------------------------
            // 3. HEURE
            // --------------------------------

            if (task.time != null) {

                try {

                    val taskMinutes =
                        timeToMinutes(task.time)

                    val currentMinutes =
                        timeToMinutes(currentTime)

                    val difference =
                        kotlin.math.abs(
                            taskMinutes - currentMinutes
                        )

                    if (difference <= 60) {

                        score += 2

                    } else {

                        score += 1
                    }

                } catch (_: Exception) {

                    // Heure invalide :
                    // aucun point supplémentaire.
                }
            }

            score
        }
    }

    /**
     * Transforme une heure "HH:mm"
     * en nombre de minutes.
     *
     * Exemple :
     * 10:30 = 630 minutes
     */
    private fun timeToMinutes(
        time: String
    ): Int {

        val parts =
            time.split(":")

        val hours =
            parts[0].toInt()

        val minutes =
            parts[1].toInt()

        return (hours * 60) + minutes
    }
}