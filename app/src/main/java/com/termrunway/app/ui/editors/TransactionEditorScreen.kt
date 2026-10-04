package com.termrunway.app.ui.editors

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
import com.termrunway.app.data.*
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.*
import com.termrunway.app.ui.util.*

@Composable
fun TransactionEditorScreen(
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
                    onClick = { pickDate(context, date, maxDateMs = System.currentTimeMillis()) { date = it } },
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
