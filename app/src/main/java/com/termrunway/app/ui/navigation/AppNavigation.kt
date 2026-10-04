package com.termrunway.app.ui.navigation

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import com.termrunway.app.ui.editors.PlanEditorScreen
import com.termrunway.app.ui.editors.TransactionEditorScreen
import com.termrunway.app.ui.screens.activity.ActivityScreen
import com.termrunway.app.ui.screens.home.HomeScreen
import com.termrunway.app.ui.screens.insights.InsightsScreen
import com.termrunway.app.ui.screens.plan.PlanScreen
import com.termrunway.app.ui.settings.SettingsScreen
import com.termrunway.app.ui.onboarding.StartupScreen
import com.termrunway.app.ui.onboarding.WelcomeScreen

private enum class RootTab(val label: String) {
    HOME("Home"), PLAN("Plan"), ACTIVITY("Activity"), INSIGHTS("Insights")
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AppNavigation(viewModel: TermRunwayViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    if (!state.preferencesLoaded || !state.dataLoaded) {
        StartupScreen()
        return
    }

    if (state.name.isBlank()) {
        WelcomeScreen(onSave = viewModel::setName)
        return
    }

    var tab by rememberSaveable { mutableStateOf(RootTab.HOME) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var transactionOpen by rememberSaveable { mutableStateOf(false) }
    var selectedTransactionId by rememberSaveable { mutableLongStateOf(0L) }
    var planEditorOpen by rememberSaveable { mutableStateOf(false) }
    val selectedTransaction = state.transactions.firstOrNull { it.id == selectedTransactionId }

    if (settingsOpen) {
        SettingsScreen(
            state = state,
            onBack = { settingsOpen = false },
            onTheme = viewModel::setTheme,
            onName = viewModel::setName,
            onAddCategory = viewModel::addCategory,
            onDeleteCategory = viewModel::deleteCategory,
            onClearData = { viewModel.clearAll { settingsOpen = false } },
            onExport = { uri -> viewModel.exportBackup(context.contentResolver, uri) {} },
            onRestore = { uri -> viewModel.restoreBackup(context.contentResolver, uri) {} }
        )
        return
    }

    if (transactionOpen) {
        TransactionEditorScreen(
            existing = selectedTransaction,
            type = selectedTransaction?.type ?: TransactionType.EXPENSE,
            categories = state.categories,
            activePlan = state.activePlan,
            planIncomes = state.plannedIncomes,
            planExpenses = state.plannedExpenses,
            allTransactions = state.transactions,
            onBack = {
                transactionOpen = false
                selectedTransactionId = 0L
            },
            onSave = {
                if (selectedTransaction == null) {
                    viewModel.addTransaction(it) {
                        transactionOpen = false
                        selectedTransactionId = 0L
                    }
                } else {
                    viewModel.updateTransaction(it) {
                        transactionOpen = false
                        selectedTransactionId = 0L
                    }
                }
            },
            onDelete = {
                viewModel.deleteTransaction(it) {
                    transactionOpen = false
                    selectedTransactionId = 0L
                }
            }
        )
        return
    }

    if (planEditorOpen) {
        PlanEditorScreen(
            existing = state.activePlan,
            existingIncome = state.plannedIncomes,
            existingExpenses = state.plannedExpenses,
            expenseCategories = state.expenseCategories,
            onBack = { planEditorOpen = false },
            onDelete = { id ->
                viewModel.deletePlan(id) { planEditorOpen = false }
            },
            onSave = { plan, income, expense ->
                viewModel.savePlan(
                    existingId = state.activePlan?.id,
                    name = plan.name,
                    startMs = plan.startMs,
                    endMs = plan.endMs,
                    startingMoneyPaise = plan.startingMoneyPaise,
                    incomes = income,
                    expenses = expense
                ) { planEditorOpen = false }
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar {
                RootTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = {
                            Icon(
                                when (item) {
                                    RootTab.HOME -> Icons.Outlined.Wallet
                                    RootTab.PLAN -> Icons.Outlined.AutoGraph
                                    RootTab.ACTIVITY -> Icons.Outlined.Analytics
                                    RootTab.INSIGHTS -> Icons.Outlined.Insights
                                },
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(58.dp)
                    .clickable {
                        selectedTransactionId = 0L
                        transactionOpen = true
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add transaction",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                RootTab.HOME -> HomeScreen(
                    state = state,
                    trackingMode = state.trackingMode,
                    onSettings = { settingsOpen = true },
                    onTrackingMode = viewModel::setTrackingMode,
                    onAdd = { transactionOpen = true },
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    },
                    onPlanEdit = { planEditorOpen = true }
                )
                RootTab.PLAN -> PlanScreen(
                    state = state,
                    onSettings = { settingsOpen = true },
                    onCreateOrEdit = { planEditorOpen = true },
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    }
                )
                RootTab.ACTIVITY -> ActivityScreen(
                    state = state,
                    onSettings = { settingsOpen = true },
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    }
                )
                RootTab.INSIGHTS -> InsightsScreen(
                    state = state,
                    onSettings = { settingsOpen = true }
                )
            }
        }
    }
}
