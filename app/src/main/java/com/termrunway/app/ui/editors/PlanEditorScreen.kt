package com.termrunway.app.ui.editors

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
import com.termrunway.app.data.*
import com.termrunway.app.ui.components.*
import com.termrunway.app.ui.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
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

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (existing == null) "Create plan" else "Edit plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (categoryChoices.isEmpty()) {
                    OutlinedTextField(
                        name,
                        { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(nameLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
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
