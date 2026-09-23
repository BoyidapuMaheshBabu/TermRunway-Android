package com.termrunway.app.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import com.termrunway.app.data.BackupManager
import com.termrunway.app.data.BackupPreview
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseStorage
import com.termrunway.app.data.Income
import com.termrunway.app.data.IncomeStorage
import com.termrunway.app.data.UserProfileStorage
import com.termrunway.app.ui.activity.ActivityScreen
import com.termrunway.app.ui.activity.TransactionEditRequest
import com.termrunway.app.ui.components.TransactionEditor
import com.termrunway.app.ui.components.TransactionType
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.insights.InsightsScreen
import com.termrunway.app.ui.navigation.MainBottomBar
import com.termrunway.app.ui.navigation.MainTab
import com.termrunway.app.ui.settings.SettingsScreen
import com.termrunway.app.ui.settings.SettingsSection
import com.termrunway.app.ui.settings.ThemeChoice
import com.termrunway.app.ui.theme.TermRunwayTheme

@Composable
fun TermRunwayApp() {
    val context = LocalContext.current.applicationContext
    val expenseStorage = remember { ExpenseStorage(context) }
    val incomeStorage = remember { IncomeStorage(context) }
    val profileStorage = remember { UserProfileStorage(context) }
    val backupManager = remember { BackupManager(context, profileStorage, expenseStorage, incomeStorage) }

    var username by remember { mutableStateOf(profileStorage.username()) }
    var expenses by remember { mutableStateOf(expenseStorage.loadExpenses()) }
    var incomes by remember { mutableStateOf(incomeStorage.loadIncomes()) }
    var dailyLimitCents by remember { mutableStateOf(profileStorage.dailyLimitCents()) }
    var themeName by remember { mutableStateOf(profileStorage.theme()) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var settingsSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    var editorType by remember { mutableStateOf<TransactionType?>(null) }
    var editRequest by remember { mutableStateOf<TransactionEditRequest?>(null) }
    var deleteAllDialog by rememberSaveable { mutableStateOf(false) }
    var backupPreview by remember { mutableStateOf<BackupPreview?>(null) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val snackbarHost = remember { SnackbarHostState() }

    fun refresh() {
        expenses = expenseStorage.loadExpenses()
        incomes = incomeStorage.loadIncomes()
        username = profileStorage.username()
        dailyLimitCents = profileStorage.dailyLimitCents()
        themeName = profileStorage.theme()
    }

    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(backupManager.exportJson()) }
                    ?: error("Cannot open backup destination.")
                profileStorage.setLastBackupName(uri.lastPathSegment.orEmpty())
                message = "Backup created successfully."
            }.onFailure { message = "Backup failed. Current data was not changed." }
        }
    }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val preview = backupManager.preview(uri)
            if (preview == null) message = "That file is not a valid TermRunway backup."
            else {
                restoreUri = uri
                backupPreview = preview
            }
        }
    }

    LaunchedEffect(message) {
        message?.let { snackbarHost.showSnackbar(it); message = null }
    }

    if (username.isBlank()) {
        UsernameSetup(onDone = {
            profileStorage.setUsername(it)
            username = profileStorage.username()
        })
        return
    }

    TermRunwayTheme(
        darkTheme = when (themeName) {
            "LIGHT" -> false
            "DARK" -> true
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        },
        dynamicColor = false
    ) {
        if (settingsSection != null) {
            SettingsScreen(
                section = settingsSection!!,
                username = username,
                dailyLimitCents = dailyLimitCents,
                theme = when (themeName) { "LIGHT" -> ThemeChoice.LIGHT; "DARK" -> ThemeChoice.DARK; else -> ThemeChoice.SYSTEM },
                lastBackupName = profileStorage.lastBackupName(),
                onBack = { settingsSection = null },
                onOpenProfile = { settingsSection = SettingsSection.PROFILE },
                onOpenBackup = { settingsSection = SettingsSection.BACKUP },
                onSaveUsername = { profileStorage.setUsername(it); refresh(); message = "Username updated." },
                onSaveDailyLimit = { profileStorage.setDailyLimitCents(it); refresh(); message = "Daily limit updated." },
                onThemeChange = {
                    profileStorage.setTheme(it.name)
                    themeName = it.name
                },
                onCreateBackup = {
                    val safe = sanitizeFileName(username).ifBlank { "User" }
                    createBackup.launch("TermRunway_" + safe + "_" + backupDate() + ".json")
                },
                onRestoreBackup = { pickBackup.launch(arrayOf("application/json", "text/plain", "application/*+json")) },
                onDeleteAll = { deleteAllDialog = true }
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHost) },
                bottomBar = {
                    MainBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onAddTransaction = { editorType = TransactionType.EXPENSE }
                    )
                },
                floatingActionButton = {}
            ) { padding ->
                when (selectedTab) {
                    MainTab.HOME -> HomeScreen(
                        username = username,
                        expenses = expenses,
                        incomes = incomes,
                        dailyLimitCents = dailyLimitCents,
                        onOpenTrack = { selectedTab = MainTab.TRACK },
                        onOpenInsights = { selectedTab = MainTab.INSIGHTS },
                        onOpenSettings = { settingsSection = SettingsSection.MAIN },
                        onAddExpense = { editorType = TransactionType.EXPENSE },
                        onAddIncome = { editorType = TransactionType.INCOME },
                        modifier = Modifier.padding(padding)
                    )
                    MainTab.TRACK -> ActivityScreen(
                        expenses = expenses,
                        incomes = incomes,
                        onSettings = { settingsSection = SettingsSection.MAIN },
                        onAddExpense = { editorType = TransactionType.EXPENSE },
                        onAddIncome = { editorType = TransactionType.INCOME },
                        onEdit = { editRequest = it },
                        modifier = Modifier.padding(padding)
                    )
                    MainTab.INSIGHTS -> InsightsScreen(
                        expenses = expenses,
                        incomes = incomes,
                        onSettings = { settingsSection = SettingsSection.MAIN },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    val request = editRequest
    val type = request?.let { if (it.expense != null) TransactionType.EXPENSE else TransactionType.INCOME } ?: editorType
    if (type != null) {
        TransactionEditor(
            initialType = type,
            expense = request?.expense,
            income = request?.income,
            onDismiss = { editorType = null; editRequest = null },
            onSaveExpense = { expense ->
                expenses = if (request?.expense == null) listOf(expense) + expenses else expenses.map { if (it.id == expense.id) expense else it }
                runCatching { expenseStorage.saveExpenses(expenses); message = if (request == null) "Expense added." else "Expense updated."; editorType = null; editRequest = null }
                    .onFailure { refresh(); message = "Could not save the expense." }
            },
            onSaveIncome = { income ->
                incomes = if (request?.income == null) listOf(income) + incomes else incomes.map { if (it.id == income.id) income else it }
                runCatching { incomeStorage.saveIncomes(incomes); message = if (request == null) "Income added." else "Income updated."; editorType = null; editRequest = null }
                    .onFailure { refresh(); message = "Could not save the income." }
            },
            onDeleteExpense = if (request?.expense != null) {
                {
                    expenses = expenses.filterNot { it.id == request.expense!!.id }
                    runCatching { expenseStorage.saveExpenses(expenses); message = "Expense deleted."; editRequest = null }.onFailure { refresh(); message = "Could not delete the expense." }
                }
            } else null,
            onDeleteIncome = if (request?.income != null) {
                {
                    incomes = incomes.filterNot { it.id == request.income!!.id }
                    runCatching { incomeStorage.saveIncomes(incomes); message = "Income deleted."; editRequest = null }.onFailure { refresh(); message = "Could not delete the income." }
                }
            } else null
        )
    }

    if (deleteAllDialog) {
        AlertDialog(
            onDismissRequest = { deleteAllDialog = false },
            title = { Text("Delete all local data?") },
            text = { Text("Your username, expenses, income and settings will be removed from this device. Create a backup first if you may need them later.") },
            confirmButton = {
                TextButton(onClick = {
                    backupManager.clearAllData()
                    deleteAllDialog = false
                    settingsSection = null
                    selectedTab = MainTab.HOME
                    refresh()
                    message = "All local data deleted."
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteAllDialog = false }) { Text("Cancel") } }
        )
    }

    if (backupPreview != null && restoreUri != null) {
        val preview = backupPreview!!
        AlertDialog(
            onDismissRequest = { backupPreview = null; restoreUri = null },
            title = { Text("Restore backup?") },
            text = { Text("Backup user: " + preview.username.ifBlank { "Unknown" } + "\nExpenses: " + preview.expenseCount + "\nIncome: " + preview.incomeCount + "\n\nRestoring replaces current local data on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { backupManager.restore(restoreUri!!) }
                        .onSuccess { refresh(); backupPreview = null; restoreUri = null; settingsSection = null; selectedTab = MainTab.HOME; message = "Backup restored successfully." }
                        .onFailure { backupPreview = null; restoreUri = null; message = "Restore failed. Current data was kept." }
                }) { Text("Replace & Restore") }
            },
            dismissButton = { TextButton(onClick = { backupPreview = null; restoreUri = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun UsernameSetup(onDone: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text("Welcome to TermRunway", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Text("Let's start with your Daily Tracker.", modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 30) value = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        androidx.compose.material3.Button(
            onClick = { onDone(value.trim()) },
            enabled = value.trim().isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        ) { Text("Continue") }
    }
}

private fun sanitizeFileName(value: String): String =
    value.trim().replace(Regex("[^A-Za-z0-9 _-]"), "").replace(Regex("\\s+"), "_").trim('_', ' ', '-')

private fun backupDate(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
