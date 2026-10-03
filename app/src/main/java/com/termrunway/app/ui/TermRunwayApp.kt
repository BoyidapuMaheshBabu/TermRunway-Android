package com.termrunway.app.ui

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private enum class RootTab(val label: String) {
    HOME("Home"), PLAN("Plan"), ACTIVITY("Activity"), INSIGHTS("Insights")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermRunwayApp(viewModel: TermRunwayViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    if (!state.preferencesLoaded) {
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
    var planMode by rememberSaveable { mutableStateOf(false) }
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogoMark(Modifier.size(30.dp))
                        Text(
                            "TermRunway",
                            modifier = Modifier.padding(start = 9.dp),
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
        Box(modifier = Modifier.padding(innerPadding)) {
            when (tab) {
                RootTab.HOME -> HomeScreen(
                    state = state,
                    planMode = planMode,
                    onPlanMode = { planMode = it },
                    onAdd = { transactionOpen = true },
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    },
                    onPlanEdit = { planEditorOpen = true }
                )
                RootTab.PLAN -> PlanScreen(
                    state = state,
                    onCreateOrEdit = { planEditorOpen = true },
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    }
                )
                RootTab.ACTIVITY -> ActivityScreen(
                    state = state,
                    onTransaction = {
                        selectedTransactionId = it.id
                        transactionOpen = true
                    }
                )
                RootTab.INSIGHTS -> InsightsScreen(state)
            }
        }
    }
}

@Composable
private fun StartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {}
}

@Composable
private fun WelcomeScreen(onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogoMark(Modifier.size(82.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                "Welcome to TermRunway",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Understand what happened to your money today, and plan what happens next.",
                color = RunwayMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 60) name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your name") },
                supportingText = { Text("TermRunway keeps your financial data on this device.") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onSave(name.trim()) },
                enabled = name.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Start using TermRunway", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun HomeScreen(
    state: AppUiState,
    planMode: Boolean,
    onPlanMode: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onTransaction: (Transaction) -> Unit,
    onPlanEdit: () -> Unit
) {
    val metrics = state.activePlan?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp)) {
                Text(
                    "Hi, " + state.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(todayLabel(), color = RunwayMuted)
            }
        }

        item { ModeToggle(selectedPlan = planMode, onChange = onPlanMode) }

        if (!planMode) {
            item {
                MainBalanceCard(
                    balance = state.balancePaise,
                    income = state.totalIncomePaise,
                    expense = state.totalExpensePaise
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AmountCard("Spent today", state.todayExpensePaise, RunwayRed, Modifier.weight(1f))
                    AmountCard("Income today", state.todayIncomePaise, RunwayMint, Modifier.weight(1f))
                }
            }
            item {
                QuickAddCard(
                    "Record a transaction",
                    "Add income or an expense in a few taps.",
                    onAdd
                )
            }
            item { SectionTitle("Recent activity") }
            val recent = state.transactions.take(5)
            if (recent.isEmpty()) {
                item {
                    EmptyState(
                        "No transactions yet",
                        "Start by recording the first thing that happened to your money.",
                        "Add transaction",
                        onAdd
                    )
                }
            } else {
                items(recent, key = { it.id }) { tx ->
                    TransactionRow(tx, onClick = { onTransaction(tx) })
                }
            }
            if (state.activePlan == null) {
                item {
                    OutlinedCard(
                        onClick = onPlanEdit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text("Want to plan a semester?", fontWeight = FontWeight.Bold)
                                Text("Create a Plan Tracking runway when you're ready.", color = RunwayMuted)
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        } else {
            if (state.activePlan == null) {
                item { PlanEmptyCard(onCreate = onPlanEdit) }
            } else {
                item {
                    PlanHeroCard(
                        plan = state.activePlan,
                        metrics = metrics,
                        onEdit = onPlanEdit
                    )
                }
                if (metrics != null) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AmountCard(
                                "Remaining",
                                metrics.actualRemainingPaise,
                                if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed,
                                Modifier.weight(1f)
                            )
                            AmountCard(
                                "Per remaining day",
                                metrics.availablePerDayPaise,
                                if (metrics.availablePerDayPaise >= 0) RunwayBlue else RunwayRed,
                                Modifier.weight(1f)
                            )
                        }
                    }
                    item { GuidanceCard(metrics) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AmountCard("Expected remaining", metrics.expectedRemainingPaise, modifier = Modifier.weight(1f))
                            AmountCard("Actual spent", metrics.actualExpensePaise, RunwayRed, Modifier.weight(1f))
                        }
                    }
                }
                item {
                    SectionTitle(
                        "Recent activity",
                        action = { TextButton(onClick = onAdd) { Text("Add") } }
                    )
                }
                items(
                    state.transactions.filter { tx ->
                        val p = state.activePlan
                        p != null && tx.dateMs in p.startMs..endOfDay(p.endMs)
                    }.take(4),
                    key = { it.id }
                ) { tx ->
                    TransactionRow(tx, onClick = { onTransaction(tx) })
                }
            }
        }
    }
}

@Composable
private fun MainBalanceCard(balance: Long, income: Long, expense: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Recorded balance", color = RunwayMuted, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            MoneyText(balance, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                MiniAmount("Income", income, RunwayMint)
                MiniAmount("Expenses", expense, RunwayRed)
            }
        }
    }
}

@Composable
private fun MiniAmount(label: String, amount: Long, color: Color) {
    Column {
        Text(label, color = RunwayMuted, style = MaterialTheme.typography.labelSmall)
        MoneyText(amount, prefixPlus = label == "Income", color = color)
    }
}

@Composable
private fun PlanHeroCard(plan: FinancialPlan, metrics: com.termrunway.app.data.PlanMetrics?, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(
                        dateLabel(plan.startMs) + " → " + dateLabel(plan.endMs),
                        color = RunwayMuted
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit plan") }
            }
            if (metrics != null) {
                Text("Actual remaining", color = RunwayMuted, style = MaterialTheme.typography.labelLarge)
                MoneyText(
                    metrics.actualRemainingPaise,
                    color = if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(metrics.status) })
                    AssistChip(onClick = {}, label = { Text(metrics.daysRemaining.toString() + " days left") })
                }
                ProgressAmountBar(
                    value = metrics.actualExpensePaise,
                    maxValue = (metrics.actualExpensePaise + metrics.actualRemainingPaise.coerceAtLeast(0)).coerceAtLeast(1),
                    label = "Money used"
                )
            }
        }
    }
}

@Composable
private fun GuidanceCard(metrics: com.termrunway.app.data.PlanMetrics) {
    val color = when (metrics.status) {
        "Overdrawn", "Above plan" -> RunwayRed
        "Below plan" -> RunwayBlue
        else -> RunwayMint
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.AutoGraph, null, tint = color)
            Column(Modifier.padding(start = 12.dp)) {
                Text(metrics.status, fontWeight = FontWeight.Bold)
                Text(metrics.guidance, color = RunwayMuted, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun PlanEmptyCard(onCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Build your runway", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(
                "Set a period, money available now, expected income and planned expenses. TermRunway will turn that into practical daily guidance.",
                color = RunwayMuted
            )
            Button(onClick = onCreate) { Text("Create plan") }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            TransactionIcon(transaction.category, transaction.type == TransactionType.INCOME)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    transaction.description.ifBlank { transaction.category },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (transaction.description.isBlank()) dateLabel(transaction.dateMs)
                    else transaction.category + " · " + dateLabel(transaction.dateMs),
                    color = RunwayMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            MoneyText(
                transaction.amountPaise,
                prefixPlus = transaction.type == TransactionType.INCOME,
                color = if (transaction.type == TransactionType.INCOME) RunwayMint else RunwayRed
            )
        }
    }
}

@Composable
private fun ActivityScreen(state: AppUiState, onTransaction: (Transaction) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(0) }
    var dateWindow by rememberSaveable { mutableStateOf(0) }

    val filtered = remember(state.transactions, query, mode, dateWindow) {
        val cutoff = if (dateWindow > 0) {
            startOfDay(addDays(System.currentTimeMillis(), -(dateWindow - 1)))
        } else 0L
        state.transactions.filter { tx ->
            val modeMatch = when (mode) {
                1 -> tx.type == TransactionType.INCOME
                2 -> tx.type == TransactionType.EXPENSE
                else -> true
            }
            val queryMatch = query.isBlank() ||
                tx.category.contains(query, true) ||
                tx.description.contains(query, true)
            modeMatch && queryMatch && (dateWindow == 0 || tx.dateMs >= cutoff)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Activity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search transactions") },
                singleLine = true
            )
        }
        item {
            FilterChipRow(
                values = listOf("All", "Income", "Expense"),
                selected = mode,
                onSelected = { mode = it }
            )
        }
        item {
            FilterChipRow(
                values = listOf("All time", "7 days", "30 days", "90 days"),
                selected = when (dateWindow) { 0 -> 0; 7 -> 1; 30 -> 2; else -> 3 },
                onSelected = { dateWindow = listOf(0, 7, 30, 90)[it] }
            )
        }
        if (filtered.isEmpty()) {
            item { EmptyState("Nothing here", "Try a different period, filter or search term.") }
        } else {
            items(filtered, key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { onTransaction(tx) })
            }
        }
    }
}

@Composable
private fun FilterChipRow(values: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEachIndexed { index, value ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelected(index) },
                label = { Text(value) }
            )
        }
    }
}

@Composable
private fun InsightsScreen(state: AppUiState) {
    var window by rememberSaveable { mutableStateOf(7) }
    val start = startOfDay(addDays(System.currentTimeMillis(), -(window - 1)))
    val income = FinancialCalculator.rangeIncome(state.transactions, start, System.currentTimeMillis())
    val expense = FinancialCalculator.rangeExpense(state.transactions, start, System.currentTimeMillis())
    val categories = FinancialCalculator.categoryTotals(
        state.transactions,
        TransactionType.EXPENSE,
        start,
        System.currentTimeMillis()
    )
    val maxCategory = categories.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp)) {
                Text("Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("See the patterns behind your money.", color = RunwayMuted)
            }
        }
        item {
            FilterChipRow(
                values = listOf("7 days", "30 days", "90 days"),
                selected = when (window) { 7 -> 0; 30 -> 1; else -> 2 },
                onSelected = { window = listOf(7, 30, 90)[it] }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AmountCard("Income", income, RunwayMint, Modifier.weight(1f))
                AmountCard("Spent", expense, RunwayRed, Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Money pulse", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    MoneyPulseChart(state.transactions, start)
                }
            }
        }
        item { SectionTitle("Where your money went") }
        if (categories.isEmpty()) {
            item {
                EmptyState(
                    "No expenses in this period",
                    "Once you record expenses, category patterns will appear here."
                )
            }
        } else {
            items(categories.entries.toList(), key = { it.key }) { entry ->
                val name = entry.key
                val value = entry.value
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(moneyString(value), fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { (value.toDouble() / maxCategory.toDouble()).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        state.activePlan?.let { plan ->
            val metrics = FinancialCalculator.planMetrics(
                plan,
                state.plannedIncomes,
                state.plannedExpenses,
                state.transactions
            )
            item { SectionTitle("Plan view") }
            item { PlanComparePanel(metrics) }
        }
    }
}

@Composable
private fun MoneyPulseChart(transactions: List<Transaction>, startMs: Long) {
    val days = ((startOfDay(System.currentTimeMillis()) - startMs) / DAY).toInt() + 1
    val values = (0 until days).map { offset ->
        val day = addDays(startMs, offset)
        FinancialCalculator.dayExpense(transactions, day)
    }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
        val width = size.width
        val height = size.height
        val horizontal = if (values.size <= 1) width else width / (values.size - 1)
        var previous: Offset? = null
        values.forEachIndexed { index, value ->
            val x = index * horizontal
            val y = height - (value.toDouble() / maxValue.toDouble() * (height - 20)).toFloat()
            val point = Offset(x, y)
            previous?.let {
                drawLine(
                    color = RunwayRed,
                    start = it,
                    end = point,
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(RunwayRed, radius = 5f, center = point)
            previous = point
        }
        drawLine(
            color = RunwayMuted,
            start = Offset(0f, height - 1f),
            end = Offset(width, height - 1f),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun PlanComparePanel(metrics: com.termrunway.app.data.PlanMetrics) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Plan vs actual", fontWeight = FontWeight.Bold)
                    Text(metrics.guidance, color = RunwayMuted, style = MaterialTheme.typography.bodySmall)
                }
                AssistChip(onClick = {}, label = { Text(metrics.status) })
            }
            CompareLine("Expected spent to date", metrics.expectedSpendToDatePaise, metrics.actualExpensePaise)
            CompareLine("Expected remaining", metrics.expectedRemainingPaise, metrics.actualRemainingPaise)
        }
    }
}

@Composable
private fun CompareLine(label: String, expected: Long, actual: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row {
            Text(label, Modifier.weight(1f), color = RunwayMuted)
            Text(moneyString(actual), fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = {
                (abs(actual).toDouble() / max(abs(expected), abs(actual)).coerceAtLeast(1).toDouble())
                    .toFloat().coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionEditorScreen(
    existing: Transaction?,
    type: TransactionType,
    categories: List<com.termrunway.app.data.Category>,
    activePlan: FinancialPlan?,
    planIncomes: List<PlannedIncome>,
    planExpenses: List<PlannedExpense>,
    allTransactions: List<Transaction>,
    onBack: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: (Long) -> Unit
) {
    var currentType by rememberSaveable(existing?.id) { mutableStateOf(existing?.type ?: type) }
    var amount by rememberSaveable(existing?.id) { mutableStateOf(existing?.amountPaise?.let(::moneyInput) ?: "") }
    var category by rememberSaveable(existing?.id) {
        mutableStateOf(existing?.category ?: categories.firstOrNull()?.name.orEmpty())
    }
    var description by rememberSaveable(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var date by rememberSaveable(existing?.id) { mutableLongStateOf(existing?.dateMs ?: System.currentTimeMillis()) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val previewTransaction = remember(currentType, amount, category, description, date, existing?.id) {
        Transaction(
            id = existing?.id ?: 0L,
            type = currentType,
            amountPaise = parseMoney(amount),
            category = category,
            description = description.trim(),
            dateMs = date,
            createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis()
        )
    }
    val previewMetrics = activePlan?.let { plan ->
        val withoutExisting = if (existing == null) allTransactions else allTransactions.filter { it.id != existing.id }
        FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = planIncomes,
            plannedExpenses = planExpenses,
            transactions = if (previewTransaction.amountPaise > 0) withoutExisting + previewTransaction else withoutExisting
        )
    }

    if (showDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete transaction?") },
            text = { Text("This will remove the record and update every related total.") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDelete(existing.id)
                }) { Text("Delete", color = RunwayRed) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }

    val activeCategories = if (currentType == TransactionType.INCOME) {
        categories.filter { it.type == CategoryType.INCOME }
    } else {
        categories.filter { it.type == CategoryType.EXPENSE }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (existing == null) "Add transaction" else "Edit transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Outlined.Delete, "Delete", tint = RunwayRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        currentType == TransactionType.EXPENSE,
                        {
                            currentType = TransactionType.EXPENSE
                            category = categories.firstOrNull { it.type == CategoryType.EXPENSE }?.name.orEmpty()
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        currentType == TransactionType.INCOME,
                        {
                            currentType = TransactionType.INCOME
                            category = categories.firstOrNull { it.type == CategoryType.INCOME }?.name.orEmpty()
                        },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Text("Category", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                CategorySelector(activeCategories, category) { category = it }
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    minLines = 2,
                    maxLines = 3
                )
            }
            item {
                OutlinedButton(
                    onClick = { pickDate(context, date) { date = it } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Text(dateLabel(date))
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (currentType == TransactionType.EXPENSE) "Impact" else "Credit",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (currentType == TransactionType.EXPENSE) {
                                "This expense reduces your recorded balance."
                            } else {
                                "This income increases your recorded balance."
                            },
                            color = RunwayMuted
                        )
                        if (previewMetrics != null && previewTransaction.amountPaise > 0) {
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Text("Plan preview", fontWeight = FontWeight.Bold)
                            Row {
                                Text("Remaining after this", Modifier.weight(1f))
                                MoneyText(
                                    previewMetrics.actualRemainingPaise,
                                    color = if (previewMetrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed
                                )
                            }
                            if (previewMetrics.daysRemaining > 0) {
                                Row {
                                    Text("Available per remaining day", Modifier.weight(1f))
                                    MoneyText(
                                        previewMetrics.availablePerDayPaise,
                                        color = if (previewMetrics.availablePerDayPaise >= 0) RunwayBlue else RunwayRed
                                    )
                                }
                            }
                            Text(
                                previewMetrics.status + " · " + previewMetrics.guidance,
                                color = RunwayMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            Transaction(
                                id = existing?.id ?: 0,
                                type = currentType,
                                amountPaise = parseMoney(amount),
                                category = category,
                                description = description.trim(),
                                dateMs = date,
                                createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Outlined.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (existing == null) "Save transaction" else "Save changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditorScreen(
    existing: FinancialPlan?,
    existingIncome: List<PlannedIncome>,
    existingExpenses: List<PlannedExpense>,
    expenseCategories: List<com.termrunway.app.data.Category>,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onSave: (FinancialPlan, List<PlannedIncome>, List<PlannedExpense>) -> Unit
) {
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var starting by rememberSaveable(existing?.id) { mutableStateOf(existing?.startingMoneyPaise?.let(::moneyInput) ?: "") }
    var startMs by rememberSaveable(existing?.id) { mutableLongStateOf(existing?.startMs ?: System.currentTimeMillis()) }
    var endMs by rememberSaveable(existing?.id) {
        mutableLongStateOf(existing?.endMs ?: addDays(System.currentTimeMillis(), 89))
    }

    val incomes = remember(existing?.id) { mutableStateListOf<PlannedIncome>().also { it.addAll(existingIncome) } }
    val expenses = remember(existing?.id) { mutableStateListOf<PlannedExpense>().also { it.addAll(existingExpenses) } }

    var addIncomeDialog by rememberSaveable { mutableStateOf(false) }
    var addExpenseDialog by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val expectedIncome = incomes.sumOf { it.amountPaise }
    val plannedExpense = expenses.sumOf { it.amountPaise }
    val expectedRemaining = parseMoney(starting) + expectedIncome - plannedExpense

    if (addIncomeDialog) {
        EntryDialog(
            title = "Expected income",
            categoryChoices = emptyList(),
            category = "",
            amount = "",
            nameLabel = "Source",
            onDismiss = { addIncomeDialog = false },
            onConfirm = { source, _, amount ->
                if (parseMoney(amount) > 0 && source.isNotBlank()) {
                    incomes += PlannedIncome(
                        planId = existing?.id ?: 0,
                        source = source,
                        amountPaise = parseMoney(amount),
                        expectedDateMs = startMs
                    )
                }
                addIncomeDialog = false
            }
        )
    }


    if (showDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this plan?") },
            text = {
                Text("The plan and its expected income/expense entries will be removed. Actual transactions will remain untouched.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDelete(existing.id)
                }) {
                    Text("Delete", color = RunwayRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }

    if (addExpenseDialog) {
        EntryDialog(
            title = "Planned expense",
            categoryChoices = expenseCategories.map { it.name },
            category = expenseCategories.firstOrNull()?.name.orEmpty(),
            amount = "",
            nameLabel = "Category",
            onDismiss = { addExpenseDialog = false },
            onConfirm = { _, category, amount ->
                if (parseMoney(amount) > 0 && category.isNotBlank()) {
                    expenses += PlannedExpense(
                        planId = existing?.id ?: 0,
                        category = category,
                        amountPaise = parseMoney(amount),
                        expectedDateMs = startMs
                    )
                }
                addExpenseDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (existing == null) "Create plan" else "Edit plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Plan name") },
                    placeholder = { Text("5th Semester") },
                    singleLine = true
                )
            }
            item {
                Text("Plan dates", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pickDate(context, startMs) { startMs = it; if (endMs < it) endMs = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text(dateLabel(startMs), maxLines = 1) }
                    OutlinedButton(
                        onClick = { pickDate(context, endMs) { if (it >= startMs) endMs = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text(dateLabel(endMs), maxLines = 1) }
                }
            }
            item {
                OutlinedTextField(
                    value = starting,
                    onValueChange = { starting = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Money available now (₹)") },
                    supportingText = { Text("Money already in hand at the beginning of the plan.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Live preview", fontWeight = FontWeight.Bold)
                        Row {
                            Text("Expected income", Modifier.weight(1f))
                            Text(moneyString(expectedIncome), fontWeight = FontWeight.Bold)
                        }
                        Row {
                            Text("Planned expenses", Modifier.weight(1f))
                            Text(moneyString(plannedExpense), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()
                        Row {
                            Text("Expected remaining", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text(
                                moneyString(expectedRemaining),
                                fontWeight = FontWeight.Black,
                                color = if (expectedRemaining >= 0) RunwayMint else RunwayRed
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(
                    "Expected income",
                    action = { TextButton(onClick = { addIncomeDialog = true }) { Text("Add") } }
                )
            }
            if (incomes.isEmpty()) {
                item { EmptyState("No expected income", "Add the money you expect to receive during this period.") }
            } else {
                items(
                    incomes,
                    key = { "i-" + it.source + "-" + it.amountPaise + "-" + it.expectedDateMs }
                ) { item ->
                    PlanEntryRow(
                        title = item.source,
                        amount = item.amountPaise,
                        subtitle = "Expected " + dateLabel(item.expectedDateMs ?: startMs),
                        color = RunwayMint,
                        onDelete = { incomes.remove(item) }
                    )
                }
            }
            item {
                SectionTitle(
                    "Planned expenses",
                    action = { TextButton(onClick = { addExpenseDialog = true }) { Text("Add") } }
                )
            }
            if (expenses.isEmpty()) {
                item { EmptyState("No planned expenses", "Add categories you expect to spend money on.") }
            } else {
                items(
                    expenses,
                    key = { "e-" + it.category + "-" + it.amountPaise + "-" + it.expectedDateMs + "-" + it.hashCode() }
                ) { item ->
                    PlanEntryRow(
                        title = item.category,
                        amount = item.amountPaise,
                        subtitle = "Planned " + dateLabel(item.expectedDateMs ?: startMs),
                        color = RunwayRed,
                        onDelete = { expenses.remove(item) }
                    )
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("How TermRunway uses this plan", fontWeight = FontWeight.Bold)
                        Text(
                            "Actual transactions inside the plan dates are compared with these expectations. Guidance changes with your remaining money and the days left.",
                            color = RunwayMuted
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(
                            FinancialPlan(
                                id = existing?.id ?: 0L,
                                name = name.trim(),
                                startMs = startMs,
                                endMs = endMs,
                                startingMoneyPaise = parseMoney(starting),
                                createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis()
                            ),
                            incomes.toList(),
                            expenses.toList()
                        )
                    },
                    enabled = name.trim().isNotEmpty() && endMs >= startMs,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(if (existing == null) "Create runway" else "Save plan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PlanEntryRow(
    title: String,
    amount: Long,
    subtitle: String,
    color: Color,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (color == RunwayMint) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                    null,
                    tint = color
                )
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = RunwayMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text(moneyString(amount), color = color, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, "Remove", tint = RunwayMuted)
            }
        }
    }
}

@Composable
private fun EntryDialog(
    title: String,
    categoryChoices: List<String>,
    category: String,
    amount: String,
    nameLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(if (categoryChoices.isEmpty()) "" else category) }
    var selectedCategory by remember { mutableStateOf(category) }
    var amountText by remember { mutableStateOf(amount) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (categoryChoices.isEmpty()) {
                    OutlinedTextField(
                        name,
                        { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(nameLabel) },
                        singleLine = true
                    )
                } else {
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                selectedCategory.ifBlank { "Choose category" },
                                Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Outlined.KeyboardArrowDown, null)
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            categoryChoices.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedCategory = it
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    amountText,
                    { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedCategory, amountText) },
                enabled = (if (categoryChoices.isEmpty()) name.trim() else selectedCategory).isNotBlank()
                    && parseMoney(amountText) > 0
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PlanScreen(
    state: AppUiState,
    onCreateOrEdit: () -> Unit,
    onTransaction: (Transaction) -> Unit
) {
    val active = state.activePlan
    val metrics = active?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }
    val completed = state.plans.filter { active == null || it.id != active.id }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Plan Tracking", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("What will happen to your money?", color = RunwayMuted)
                }
                IconButton(onClick = onCreateOrEdit) {
                    Icon(
                        if (active == null) Icons.Outlined.AutoGraph else Icons.Outlined.Edit,
                        "Plan"
                    )
                }
            }
        }

        if (active == null) {
            item { PlanEmptyCard(onCreateOrEdit) }
        } else {
            item { PlanHeroCard(active, metrics, onCreateOrEdit) }
            item { SectionTitle("Expected inputs") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AmountCard("Expected income", metrics?.totalExpectedIncomePaise ?: 0L, RunwayMint, Modifier.weight(1f))
                    AmountCard("Planned expenses", metrics?.totalPlannedExpensePaise ?: 0L, RunwayRed, Modifier.weight(1f))
                }
            }
            item { SectionTitle("Recent plan activity") }
            val planTransactions = state.transactions.filter {
                it.dateMs in active.startMs..endOfDay(active.endMs)
            }.take(5)
            if (planTransactions.isEmpty()) {
                item {
                    EmptyState(
                        "No actual activity yet",
                        "Record transactions to compare reality with your plan."
                    )
                }
            } else {
                items(planTransactions, key = { it.id }) { tx ->
                    TransactionRow(tx, { onTransaction(tx) })
                }
            }
            if (completed.isNotEmpty()) {
                item { SectionTitle("Other plans") }
                items(completed, key = { it.id }) { plan ->
                    OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(plan.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    dateLabel(plan.startMs) + " → " + dateLabel(plan.endMs),
                                    color = RunwayMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value,
                        { value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Category name") },
                        singleLine = true
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
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                OutlinedTextField(
                    editName,
                    {
                        val next = it.take(60)
                        editName = next
                        onName(next)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    singleLine = true
                )
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

private fun pickDate(context: android.content.Context, current: Long, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = current }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            onPicked(
                Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun startOfDay(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endOfDay(ms: Long): Long = startOfDay(ms) + DAY - 1

private fun addDays(ms: Long, count: Int): Long = ms + DAY * count

private fun parseMoney(value: String): Long =
    runCatching {
        val normalized = value.trim().replace(",", "")
        require(normalized.isNotEmpty())
        java.math.BigDecimal(normalized)
            .setScale(2, java.math.RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
            .coerceAtLeast(0L)
    }.getOrDefault(0L)

private fun moneyInput(paise: Long): String =
    String.format(Locale.getDefault(), "%.2f", paise / 100.0)

private fun fileDate(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())

private const val DAY = 86_400_000L
