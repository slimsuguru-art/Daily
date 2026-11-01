package com.example.daily_app

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

enum class Category {
    WORK,
    PERSONAL,
    STUDY,
    DEVELOPMENT,
    OTHER
}

data class Task(
    val id: Int,
    val title: String,
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.OTHER,
    val date: String? = null,
    val time: String? = null
)
