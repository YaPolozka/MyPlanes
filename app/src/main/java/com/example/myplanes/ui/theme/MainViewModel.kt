package com.example.myplanes.ui

import com.example.myplanes.cleanup.CleanupScheduler
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myplanes.data.AppDatabase
import com.example.myplanes.data.Quadrant
import com.example.myplanes.data.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).taskDao()

    init {
        // При запуске убираем задачи, которым больше недели после выполнения или удаления
        viewModelScope.launch {
            dao.purgeOlderThan(CleanupScheduler.cutoffMillis())
        }
    }
    // Все задачи, разложенные по четырём разделам
    val tasksByQuadrant: StateFlow<Map<Quadrant, List<Task>>> = dao.observeAll()
        .map { tasks ->
            Quadrant.entries.associateWith { quadrant ->
                tasks.filter { it.quadrant == quadrant }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Quadrant.entries.associateWith { emptyList<Task>() }
        )

    // completed = true ставит галочку, false снимает
    fun setCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch {
            dao.setCompleted(task.id, if (completed) System.currentTimeMillis() else null)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.setDeleted(task.id, System.currentTimeMillis())
        }
    }
}