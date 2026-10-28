package com.example.myplanes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myplanes.data.Quadrant
import com.example.myplanes.data.Task
import com.example.myplanes.data.isCompleted
import com.example.myplanes.data.isDeleted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddTask: (Quadrant) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val tasksByQuadrant by viewModel.tasksByQuadrant.collectAsStateWithLifecycle()
    val quadrants = Quadrant.entries
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("MyPlanes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask(quadrants[selectedTab]) }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                quadrants.forEachIndexed { index, quadrant ->
                    // В заголовке вкладки показываем число активных задач
                    val active = tasksByQuadrant[quadrant].orEmpty()
                        .count { !it.isCompleted && !it.isDeleted }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(if (active > 0) "${quadrant.title} ($active)" else quadrant.title)
                        }
                    )
                }
            }

            TaskList(
                tasks = tasksByQuadrant[quadrants[selectedTab]].orEmpty(),
                onToggleCompleted = viewModel::setCompleted,
                onDelete = viewModel::deleteTask
            )
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onToggleCompleted: (Task, Boolean) -> Unit,
    onDelete: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "В этом разделе пока нет задач",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            // Нижний отступ, чтобы кнопка «+» не закрывала последнюю задачу
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(task, onToggleCompleted, onDelete)
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onToggleCompleted: (Task, Boolean) -> Unit,
    onDelete: (Task) -> Unit
) {
    val finished = task.isCompleted || task.isDeleted
    val deadline = task.deadline
    val overdue = !finished && deadline != null && deadline < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (task.isDeleted) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompleted(task, it) },
                enabled = !task.isDeleted
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (finished) TextDecoration.LineThrough else null
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (deadline != null) {
                    Text(
                        text = "Дедлайн: ${formatDate(deadline)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (overdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                if (task.isDeleted) {
                    Text(
                        text = "Удалена",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!task.isDeleted) {
                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))