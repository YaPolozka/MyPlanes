package com.example.myplanes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplanes.data.AppDatabase
import com.example.myplanes.data.Quadrant
import com.example.myplanes.data.Task
import com.example.myplanes.reminders.ReminderScheduler
import kotlinx.coroutines.launch

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).taskDao()

    fun addTask(
        title: String,
        description: String,
        quadrant: Quadrant,
        deadline: Long?,
        reminderAt: Long?,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val id = dao.insert(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    quadrant = quadrant,
                    deadline = deadline,
                    reminderAt = reminderAt
                )
            )
            if (reminderAt != null) {
                ReminderScheduler.schedule(getApplication(), id, reminderAt)
            }
            onSaved()
        }
    }
}