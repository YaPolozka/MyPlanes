package com.example.myplanes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val quadrant: Quadrant,
    val deadline: Long? = null,      // дедлайн, миллисекунды (epoch)
    val reminderAt: Long? = null,    // когда прислать уведомление, миллисекунды (epoch)
    val completedAt: Long? = null,   // когда отмечена выполненной (null = не выполнена)
    val deletedAt: Long? = null,     // когда удалена (null = не удалена)
    val createdAt: Long = System.currentTimeMillis()
)

val Task.isCompleted: Boolean get() = completedAt != null
val Task.isDeleted: Boolean get() = deletedAt != null