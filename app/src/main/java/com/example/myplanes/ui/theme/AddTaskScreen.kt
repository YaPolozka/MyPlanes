package com.example.myplanes.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myplanes.data.Quadrant
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    initialQuadrant: Quadrant,
    onBack: () -> Unit,
    viewModel: AddTaskViewModel = viewModel()
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var quadrant by rememberSaveable { mutableStateOf(initialQuadrant) }
    var deadline by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var reminder by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }
    var saving by rememberSaveable { mutableStateOf(false) }

    // Состояние диалогов выбора даты и времени
    var showDeadlineDialog by rememberSaveable { mutableStateOf(false) }
    var showReminderDateDialog by rememberSaveable { mutableStateOf(false) }
    var showReminderTimeDialog by rememberSaveable { mutableStateOf(false) }
    var pendingReminderDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    val reminderValue = reminder
    val reminderInPast = reminderValue != null && reminderValue.isBefore(LocalDateTime.now())
    val canSave = title.isNotBlank() && !reminderInPast && !saving

    val context = LocalContext.current

    val save = {
        saving = true
        viewModel.addTask(
            title = title,
            description = description,
            quadrant = quadrant,
            deadline = deadline?.let { endOfDayMillis(it) },
            reminderAt = reminder?.let {
                it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            onSaved = onBack
        )
    }

    // Задача сохраняется в любом случае, даже если разрешение не дали
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { save() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая задача") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Раздел", style = MaterialTheme.typography.titleSmall)
            Column(modifier = Modifier.selectableGroup()) {
                Quadrant.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = quadrant == option,
                                onClick = { quadrant = option },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = quadrant == option, onClick = null)
                        Text(text = option.title, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }

            PickerRow(
                label = "Дедлайн",
                value = deadline?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                onPick = { showDeadlineDialog = true },
                onClear = { deadline = null }
            )

            PickerRow(
                label = "Уведомление",
                value = reminder?.format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                ),
                onPick = { showReminderDateDialog = true },
                onClear = { reminder = null }
            )
            if (reminderInPast) {
                Text(
                    text = "Это время уже прошло",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val needsPermission = reminder != null &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED

                    if (needsPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        save()
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }

    if (showDeadlineDialog) {
        DateDialog(
            onDismiss = { showDeadlineDialog = false },
            onConfirm = { date ->
                deadline = date
                showDeadlineDialog = false
            }
        )
    }

    // Уведомление выбираем в два шага: сначала дата, потом время
    if (showReminderDateDialog) {
        DateDialog(
            onDismiss = { showReminderDateDialog = false },
            onConfirm = { date ->
                pendingReminderDate = date
                showReminderDateDialog = false
                showReminderTimeDialog = true
            }
        )
    }

    if (showReminderTimeDialog) {
        TimeDialog(
            onDismiss = { showReminderTimeDialog = false },
            onConfirm = { hour, minute ->
                pendingReminderDate?.let { reminder = it.atTime(hour, minute) }
                showReminderTimeDialog = false
            }
        )
    }
}

@Composable
private fun PickerRow(
    label: String,
    value: String?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    OutlinedCard(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value ?: "Не задан",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (value != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        // DatePicker отдаёт полночь по UTC, переводим в обычную дату
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        text = { TimePicker(state = state) }
    )
}

// Дедлайн хранится как конец выбранного дня: задача считается просроченной только после него
private fun endOfDayMillis(date: LocalDate): Long =
    date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1