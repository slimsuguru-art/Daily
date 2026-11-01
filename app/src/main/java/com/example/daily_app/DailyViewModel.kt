package com.example.daily_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class DailyViewModel(
    application: Application
) : AndroidViewModel(application) {

    // ---------------------------------------------------------
    // BASE DE DONNÉES
    // ---------------------------------------------------------

    private val database =
        TaskDatabase.getDatabase(application)

    private val taskDao =
        database.taskDao()


    // ---------------------------------------------------------
    // LISTE DES TÂCHES
    // ---------------------------------------------------------

    private val _tasks =
        MutableStateFlow<List<Task>>(emptyList())

    val tasks: StateFlow<List<Task>> =
        _tasks.asStateFlow()


    // ---------------------------------------------------------
    // INITIALISATION
    // ---------------------------------------------------------

    init {

        observeTasks()
    }


    // ---------------------------------------------------------
    // OBSERVER LA BASE DE DONNÉES
    // ---------------------------------------------------------

    private fun observeTasks() {

        viewModelScope.launch {

            taskDao
                .getAllTasks()
                .collectLatest { entities ->

                    _tasks.value =
                        entities.map {
                            it.toTask()
                        }
                }
        }
    }


    // ---------------------------------------------------------
    // AJOUTER UNE TÂCHE
    // ---------------------------------------------------------

    fun addTask(
        title: String,
        priority: Priority,
        category: Category,
        date: String?,
        time: String?
    ) {

        viewModelScope.launch {

            val currentTasks =
                _tasks.value

            val newId =
                if (currentTasks.isEmpty()) {
                    0
                } else {
                    currentTasks.maxOf {
                        it.id
                    } + 1
                }


            val task =
                TaskEntity(

                    id = newId,

                    title = title.trim(),

                    completed = false,

                    priority = priority.name,

                    category = category.name,

                    date = date,

                    time = time
                )


            taskDao.insertTask(task)
        }
    }


    // ---------------------------------------------------------
    // MODIFIER UNE TÂCHE
    // ---------------------------------------------------------

    fun updateTask(
        taskId: Int,
        title: String,
        priority: Priority,
        category: Category,
        date: String?,
        time: String?
    ) {

        viewModelScope.launch {

            val currentTask =
                _tasks.value.firstOrNull {
                    it.id == taskId
                }

            if (currentTask != null) {

                val updatedTask =
                    TaskEntity(

                        id = taskId,

                        title = title.trim(),

                        completed =
                            currentTask.completed,

                        priority =
                            priority.name,

                        category =
                            category.name,

                        date = date,

                        time = time
                    )


                taskDao.updateTask(
                    updatedTask
                )
            }
        }
    }


    // ---------------------------------------------------------
    // SUPPRIMER UNE TÂCHE
    // ---------------------------------------------------------

    fun deleteTask(
        taskId: Int
    ) {

        viewModelScope.launch {

            val task =
                _tasks.value.firstOrNull {
                    it.id == taskId
                }

            if (task != null) {

                taskDao.deleteTask(
                    task.toTaskEntity()
                )
            }
        }
    }


    // ---------------------------------------------------------
    // TERMINER / RÉACTIVER UNE TÂCHE
    // ---------------------------------------------------------

    fun toggleTask(
        taskId: Int
    ) {

        viewModelScope.launch {

            val currentTask =
                _tasks.value.firstOrNull {
                    it.id == taskId
                }

            if (currentTask != null) {

                val updatedTask =
                    currentTask.copy(
                        completed =
                            !currentTask.completed
                    )

                taskDao.updateTask(
                    updatedTask.toTaskEntity()
                )
            }
        }
    }
}


// ---------------------------------------------------------
// CONVERSION ENTITY → TASK
// ---------------------------------------------------------

fun TaskEntity.toTask(): Task {

    return Task(

        id = id,

        title = title,

        completed = completed,

        priority =
            Priority.valueOf(priority),

        category =
            Category.valueOf(category),

        date = date,

        time = time
    )
}


// ---------------------------------------------------------
// CONVERSION TASK → ENTITY
// ---------------------------------------------------------

fun Task.toTaskEntity(): TaskEntity {

    return TaskEntity(

        id = id,

        title = title,

        completed = completed,

        priority = priority.name,

        category = category.name,

        date = date,

        time = time
    )
}