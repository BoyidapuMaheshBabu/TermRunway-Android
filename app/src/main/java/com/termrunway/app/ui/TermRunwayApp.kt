package com.termrunway.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.termrunway.app.data.AppData
import com.termrunway.app.data.TermRunwayRepository
import com.termrunway.app.data.ThemeMode
import com.termrunway.app.ui.components.EditorRequest
import com.termrunway.app.ui.components.TransactionEditor
import com.termrunway.app.ui.navigation.MainBottomBar
import com.termrunway.app.ui.navigation.MainTab
import com.termrunway.app.ui.screens.HomeScreen
import com.termrunway.app.ui.screens.InsightsScreen
import com.termrunway.app.ui.screens.SettingsScreen
import com.termrunway.app.ui.screens.SetupScreen
import com.termrunway.app.ui.screens.TrackScreen
import com.termrunway.app.ui.theme.TermRunwayTheme
import com.termrunway.app.util.startOfDay

@Composable
fun TermRunwayApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember { TermRunwayRepository(context) }

    var data by remember { mutableStateOf(repository.load()) }
    var selectedTabName by rememberSaveable { mutableStateOf(MainTab.HOME.name) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var editorRequest by remember { mutableStateOf<EditorRequest?>(null) }
    var restoreData by remember { mutableStateOf<AppData?>(null) }
    var deleteAllPending by rememberSaveable { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    fun persist(updated: AppData, successMessage: String? = null) {
        runCatching {
            repository.save(updated)
            data = updated
        }.onSuccess {
            successMessage?.let { snackbarMessage = it }
        }.onFailure {
            snackbarMessage = it.message ?: "Could not save your changes."
        }
    }

    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(repository.exportJson(data)) }
                ?: error("Could not open backup destination.")
        }.onSuccess {
            snackbarMessage = "Backup created."
        }.onFailure {
            snackbarMessage = "Backup failed. Current data was not changed."
        }
    }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Could not read the selected file.")
            restoreData = repository.parseBackup(json)
        }.onFailure {
            snackbarMessage = it.message ?: "That file is not a valid TermRunway backup."
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            snackbarMessage = null
        }
    }

    TermRunwayTheme(
        darkTheme = when (data.theme) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    ) {
        when {
            data.username.isBlank() -> {
                SetupScreen { name -> persist(data.copy(username = name)) }
            }

            settingsOpen -> {
                SettingsScreen(
                    data = data,
                    onBack = { settingsOpen = false },
                    onSaveUsername = { name -> persist(data.copy(username = name), "Profile updated.") },
                    onSaveDailyLimit = { cents -> persist(data.copy(dailyLimitCents = cents), "Daily limit updated.") },
                    onThemeChange = { theme -> persist(data.copy(theme = theme), "Theme updated.") },
                    onCreateBackup = { createBackup.launch("termrunway-backup.json") },
                    onPickBackup = { pickBackup.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    onDeleteAll = { deleteAllPending = true }
                )
            }

            else -> {
                val selectedTab = MainTab.valueOf(selectedTabName)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHost) },
                    bottomBar = { MainBottomBar(selectedTab, onTabSelected = { selectedTabName = it.name }) },
                    floatingActionButton = {
                        if (selectedTab == MainTab.TRACK) {
                            FloatingActionButton(onClick = {
                                editorRequest = EditorRequest(
                                    kind = EditorRequest.Kind.EXPENSE,
                                    defaultDateMillis = startOfDay(System.currentTimeMillis())
                                )
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add transaction")
                            }
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
                        MainTab.HOME -> HomeScreen(
                            data = data,
                            contentPadding = padding,
                            onSettings = { settingsOpen = true },
                            onOpenTrack = { selectedTabName = MainTab.TRACK.name },
                            onOpenInsights = { selectedTabName = MainTab.INSIGHTS.name },
                            onAddExpense = {
                                editorRequest = EditorRequest(
                                    kind = EditorRequest.Kind.EXPENSE,
                                    defaultDateMillis = startOfDay(System.currentTimeMillis())
                                )
                            },
                            onAddIncome = {
                                editorRequest = EditorRequest(
                                    kind = EditorRequest.Kind.INCOME,
                                    defaultDateMillis = startOfDay(System.currentTimeMillis())
                                )
                            },
                            onEdit = { editorRequest = it }
                        )

                        MainTab.TRACK -> TrackScreen(
                            data = data,
                            contentPadding = padding,
                            onSettings = { settingsOpen = true },
                            onAdd = { day -> editorRequest = EditorRequest(defaultDateMillis = day) },
                            onEdit = { editorRequest = it }
                        )

                        MainTab.INSIGHTS -> InsightsScreen(
                            data = data,
                            contentPadding = padding,
                            onSettings = { settingsOpen = true }
                        )
                    }
                }
            }
        }

        editorRequest?.let { request ->
            TransactionEditor(
                request = request,
                currentData = data,
                onDismiss = { editorRequest = null },
                onSave = { updated ->
                    persist(updated)
                    editorRequest = null
                },
                onDelete = { updated ->
                    persist(updated, "Transaction deleted.")
                    editorRequest = null
                }
            )
        }

        restoreData?.let { restored ->
            AlertDialog(
                onDismissRequest = { restoreData = null },
                title = { Text("Restore backup?") },
                text = {
                    Text(
                        "This will replace your current data with " +
                            restored.expenses.size + " expenses and " +
                            restored.incomes.size + " income records."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        restoreData = null
                        persist(restored, "Backup restored.")
                    }) { Text("Restore") }
                },
                dismissButton = {
                    TextButton(onClick = { restoreData = null }) { Text("Cancel") }
                }
            )
        }

        if (deleteAllPending) {
            AlertDialog(
                onDismissRequest = { deleteAllPending = false },
                title = { Text("Delete all local data?") },
                text = {
                    Text("This removes transactions, name, limit, and theme from this phone. A backup is the only way to recover it.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        deleteAllPending = false
                        persist(AppData(), "All local data deleted.")
                    }) { Text("Delete all") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteAllPending = false }) { Text("Cancel") }
                }
            )
        }
    }
}
