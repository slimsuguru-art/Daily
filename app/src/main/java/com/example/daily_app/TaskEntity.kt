package com.example.daily_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey
    val id: Int,

    val title: String,

    val completed: Boolean = false,

    val priority: String = "MEDIUM",

    val category: String = "OTHER",

    val date: String? = null,

    val time: String? = null
)