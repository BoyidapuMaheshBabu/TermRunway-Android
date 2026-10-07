package com.termrunway.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.CategoryType
import com.termrunway.app.notifications.NotificationHelper
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.ThemeMode
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.util.fileDate
import com.termrunway.app.ui.util.sanitizeFilename

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AppUiState,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onName: (String) -> Unit,
    onAddCategory: (String, CategoryType) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onClearData: () -> Unit,
    onExport: (android.net.Uri) -> Unit,
    onRestore: (android.net.Uri) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onDailyReminderToggle: (Boolean) -> Unit,
    onWeeklyReviewToggle: (Boolean) -> Unit,
    onMonthlyReviewToggle: (Boolean) -> Unit,
    onPlanEndingToggle: (Boolean) -> Unit,
    onReminderHourChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val createBackup = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        if (uri != null) onExport(uri)
    }
    val restoreBackup = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) onRestore(uri)
    }

    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) {
            onNotificationsToggle(true)
        }
    }

    val focusManager = LocalFocusManager.current
    var editName by rememberSaveable { mutableStateOf(state.name) }
    var addCategory by rememberSaveable { mutableStateOf(false) }
    var clearConfirm by rememberSaveable { mutableStateOf(false) }
    var categoryType by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }

    if (addCategory) {
        var value by rememberSaveable { mutableStateOf("") }
        val existingSameType = remember(categoryType, state.categories) {
            state.categories.filter { it.type == categoryType }.map { it.name.trim().lowercase() }
        }
        AlertDialog(
            onDismissRequest = { addCategory = false },
            title = { Text("Add category") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Category name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            categoryType == CategoryType.EXPENSE,
                            { categoryType = CategoryType.EXPENSE },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            categoryType == CategoryType.INCOME,
                            { categoryType = CategoryType.INCOME },
                            label = { Text("Income") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddCategory(value, categoryType)
                        addCategory = false
                    },
                    enabled = value.trim().length in 2..32 && !existingSameType.contains(value.trim().lowercase())
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { addCategory = false }) { Text("Cancel") } }
        )
    }

    if (clearConfirm) {
        AlertDialog(
            onDismissRequest = { clearConfirm = false },
            title = { Text("Clear all local data?") },
            text = {
                Text(
                    "This permanently removes transactions, plans and custom categories from this device. Export a backup first if you need a copy."
                )
            },
            confirmButton = {
                TextButton(onClick = { clearConfirm = false; onClearData() }) {
                    Text("Clear everything", color = RunwayRed)
                }
            },
            dismissButton = { TextButton(onClick = { clearConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it.take(40) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (editName.trim().isNotBlank() && editName.trim() != state.name) {
                                onName(editName.trim())
                                focusManager.clearFocus()
                            } else {
                                focusManager.clearFocus()
                            }
                        })
                    )
                    Button(
                        onClick = {
                            if (editName.trim().isNotBlank()) {
                                onName(editName.trim())
                                focusManager.clearFocus()
                            }
                        },
                        enabled = editName.trim().isNotBlank() && editName.trim() != state.name,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Save")
                    }
                }
            }

            item {
                Text(
                    "Notifications & Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                item {
                    OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text("Enable Notifications", fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Allow notifications to receive daily transaction reminders, weekly/monthly spending reviews, and active plan ending alerts.",
                                color = RunwayMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }

            item {
                SettingToggleRow(
                    title = "Allow Reminders",
                    subtitle = "Master switch for all local notification alerts",
                    checked = state.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onNotificationsToggle(enabled)
                        }
                    }
                )
            }

            if (state.notificationsEnabled) {
                item {
                    SettingToggleRow(
                        title = "Daily expense reminder",
                        subtitle = "Remind at reminder time if no transactions recorded today",
                        checked = state.dailyReminderEnabled,
                        onCheckedChange = onDailyReminderToggle
                    )
                }
                item {
                    SettingToggleRow(
                        title = "Weekly money review",
                        subtitle = "Remind on Sunday evening to review weekly cash flow",
                        checked = state.weeklyReviewEnabled,
                        onCheckedChange = onWeeklyReviewToggle
                    )
                }
                item {
                    SettingToggleRow(
                        title = "Monthly money review",
                        subtitle = "Remind at the end of the month to review monthly spending",
                        checked = state.monthlyReviewEnabled,
                        onCheckedChange = onMonthlyReviewToggle
                    )
                }
                item {
                    SettingToggleRow(
                        title = "Active Plan ending alerts",
                        subtitle = "Remind 3 days before and on the date an active plan ends",
                        checked = state.planEndingEnabled,
                        onCheckedChange = onPlanEndingToggle
                    )
                }
                item {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("Reminder time", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(19 to "7:00 PM", 20 to "8:00 PM", 21 to "9:00 PM", 22 to "10:00 PM").forEach { (hour, label) ->
                                FilterChip(
                                    selected = state.reminderHour == hour,
                                    onClick = { onReminderHourChange(hour) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChoice("System", ThemeMode.SYSTEM, state.themeMode, onTheme, Icons.Outlined.Tune)
                    ThemeChoice("Light", ThemeMode.LIGHT, state.themeMode, onTheme, Icons.Outlined.LightMode)
                    ThemeChoice("Dark", ThemeMode.DARK, state.themeMode, onTheme, Icons.Outlined.NightsStay)
                }
            }
            item {
                Text(
                    "Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                SettingsAction(
                    Icons.Outlined.Backup,
                    "Export backup",
                    "Save a complete JSON copy of your local data."
                ) {
                    createBackup.launch(sanitizeFilename(state.name, "json"))
                }
            }
            item {
                SettingsAction(
                    Icons.Outlined.Restore,
                    "Restore backup",
                    "Replace local data with a validated TermRunway JSON backup."
                ) {
                    restoreBackup.launch(arrayOf("application/json", "text/json", "text/plain"))
                }
            }
            item {
                SettingsAction(
                    Icons.Outlined.Delete,
                    "Clear all local data",
                    "Remove all financial data from this device."
                ) {
                    clearConfirm = true
                }
            }
            item {
                Text(
                    "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                OutlinedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Custom categories", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            TextButton(onClick = { addCategory = true }) { Text("Add") }
                        }
                        val custom = state.categories.filter { !it.isDefault }
                        if (custom.isEmpty()) {
                            Text("No custom categories yet.", color = RunwayMuted)
                        } else {
                            custom.forEach { category ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category.name, Modifier.weight(1f))
                                    TextButton(onClick = { onDeleteCategory(category.id) }) {
                                        Text("Remove", color = RunwayRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("About TermRunway", fontWeight = FontWeight.Bold)
                        Text("Version 1.0.0 · Offline-first student finance planner", color = RunwayMuted)
                        Text(
                            "No account, server, bank connection or financial data sync is required.",
                            color = RunwayMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = RunwayMuted, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ThemeChoice(
    label: String,
    mode: ThemeMode,
    selected: ThemeMode,
    onTheme: (ThemeMode) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    FilterChip(
        selected = mode == selected,
        onClick = { onTheme(mode) },
        leadingIcon = { Icon(icon, null) },
        label = { Text(label) }
    )
}

@Composable
private fun SettingsAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    OutlinedCard(onClick = onClick, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = RunwayMuted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Outlined.KeyboardArrowRight, null)
        }
    }
}
