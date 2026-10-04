package com.termrunway.app.ui.settings

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termrunway.app.data.CategoryType
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.AppLogoMark
import com.termrunway.app.ui.components.CategorySelector
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.ModeToggle
import com.termrunway.app.ui.components.MoneyText
import com.termrunway.app.ui.components.ProgressAmountBar
import com.termrunway.app.ui.components.QuickAddCard
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionIcon
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.components.moneyString
import com.termrunway.app.ui.components.todayLabel
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.mode.TrackingMode
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.ThemeMode
import com.termrunway.app.ui.TermRunwayViewModel
import com.termrunway.app.ui.components.*
import com.termrunway.app.ui.util.fileDate

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
    onRestore: (android.net.Uri) -> Unit
) {
    val createBackup = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        if (uri != null) onExport(uri)
    }
    val restoreBackup = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) onRestore(uri)
    }

    val focusManager = LocalFocusManager.current
    var editName by rememberSaveable { mutableStateOf(state.name) }
    var addCategory by rememberSaveable { mutableStateOf(false) }
    var clearConfirm by rememberSaveable { mutableStateOf(false) }
    var categoryType by rememberSaveable { mutableStateOf(CategoryType.EXPENSE) }

    if (addCategory) {
        var value by rememberSaveable { mutableStateOf("") }
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
                        value,
                        { value = it },
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
                    enabled = value.trim().length >= 2
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
                        onValueChange = { editName = it.take(60) },
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
                    createBackup.launch("TermRunway_Backup_" + fileDate() + ".json")
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
