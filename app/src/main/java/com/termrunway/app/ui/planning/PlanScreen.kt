@file:OptIn(ExperimentalMaterial3Api::class)
package com.termrunway.app.ui.planning

import androidx.compose.material3.ExperimentalMaterial3Api

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import com.termrunway.app.logic.DateRange
import com.termrunway.app.logic.PlanPeriodAnalysis
import com.termrunway.app.logic.TrackingPeriod
import com.termrunway.app.logic.calculatePlanAnalysis
import com.termrunway.app.logic.endOfDay
import com.termrunway.app.logic.formatRupees
import com.termrunway.app.logic.intersectRanges
import com.termrunway.app.logic.startOfDay
import com.termrunway.app.logic.trackingRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PlanScreen(
    currentPlan: TermPlan?,
    expenses: List<Expense>,
    incomes: List<Income>,
    onSave: (TermPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember(currentPlan) { mutableStateOf(currentPlan == null) }

    if (currentPlan == null || editing) {
        PlanEditor(
            currentPlan = currentPlan,
            onSave = {
                onSave(it)
                editing = false
            },
            modifier = modifier
        )
    } else {
        PlanResults(
            plan = currentPlan,
            expenses = expenses,
            incomes = incomes,
            onEdit = { editing = true },
            modifier = modifier
        )
    }
}

@Composable
private fun PlanEditor(
    currentPlan: TermPlan?,
    onSave: (TermPlan) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var startDate by remember(currentPlan) {
        mutableStateOf(currentPlan?.startDateMillis ?: System.currentTimeMillis())
    }
    var endDate by remember(currentPlan) {
        mutableStateOf(
            currentPlan?.endDateMillis
                ?: (System.currentTimeMillis() + 90L * 24L * 60L * 60L * 1000L)
        )
    }
    var startingFundsText by remember(currentPlan) {
        mutableStateOf(currentPlan?.startingFundsCents?.let(ExpenseAmount::format) ?: "")
    }
    var expectedIncomeText by remember(currentPlan) {
        mutableStateOf(currentPlan?.expectedIncomeCents?.let(ExpenseAmount::format) ?: "")
    }
    val amounts = remember(currentPlan) {
        mutableStateMapOf<String, String>().apply {
            ExpenseCategories.ALL.forEach { category ->
                val value = currentPlan?.plannedExpenses?.get(category)
                if (value != null && value > 0L) put(category, ExpenseAmount.format(value))
            }
        }
    }
    var error by remember { mutableStateOf<String?>(null) }

    val startingFunds = ExpenseAmount.parseToCents(startingFundsText) ?: 0L
    val expectedIncome = ExpenseAmount.parseToCents(expectedIncomeText) ?: 0L
    val plannedExpenses = amounts.mapNotNull { (category, value) ->
        ExpenseAmount.parseToCents(value)?.let { category to it }
    }.toMap()
    val totalPlanned = plannedExpenses.values.sum()
    val planDays = com.termrunway.app.logic.daysBetweenInclusive(startDate, endDate).coerceAtLeast(1)
    val projectedBalance = startingFunds + expectedIncome - totalPlanned
    val previewHealth = when {
        startingFunds + expectedIncome <= 0L && totalPlanned == 0L -> 100
        startingFunds + expectedIncome <= 0L -> 0
        else -> ((projectedBalance.toDouble() / (startingFunds + expectedIncome).toDouble()) * 100.0)
            .coerceIn(0.0, 100.0)
            .toInt()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentPlan == null) "Create Plan" else "Edit Plan") }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Plan the money first. The saved plan becomes the reference; actual transactions will be compared with it.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField(
                    label = "Start",
                    millis = startDate,
                    dateFormat = dateFormat,
                    onClick = {
                        showDatePicker(context, startDate) { startDate = it }
                    },
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = "End",
                    millis = endDate,
                    dateFormat = dateFormat,
                    onClick = {
                        showDatePicker(context, endDate) { endDate = it }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = startingFundsText,
                onValueChange = { startingFundsText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Available Money Now") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = expectedIncomeText,
                onValueChange = { expectedIncomeText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expected Future Income") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Text("Planned Expenses", style = MaterialTheme.typography.titleMedium)

            ExpenseCategories.ALL.forEach { category ->
                OutlinedTextField(
                    value = amounts[category] ?: "",
                    onValueChange = { amounts[category] = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(category) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Plan indication", style = MaterialTheme.typography.titleMedium)
                    Text("Plan days: " + planDays)
                    Text("Planned expenses: ₹" + ExpenseAmount.format(totalPlanned))
                    Text("Projected balance: ₹" + ExpenseAmount.format(projectedBalance))
                    Text("Healthy reference: ₹" + ExpenseAmount.format(if (planDays > 0) totalPlanned / planDays else 0L) + " / day")
                    Text("Plan health preview: " + previewHealth + "%")
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    when {
                        endDate <= startDate -> error = "End date must be after start date."
                        startingFundsText.isNotBlank() && ExpenseAmount.parseToCents(startingFundsText) == null ->
                            error = "Enter a valid positive amount for available money."
                        expectedIncomeText.isNotBlank() && ExpenseAmount.parseToCents(expectedIncomeText) == null ->
                            error = "Enter a valid positive amount for expected income."
                        totalPlanned <= 0L -> error = "Add at least one planned expense."
                        else -> {
                            error = null
                            onSave(
                                TermPlan(
                                    startDateMillis = startOfDay(startDate),
                                    endDateMillis = endOfDay(endDate),
                                    startingFundsCents = startingFunds,
                                    expectedIncomeCents = expectedIncome,
                                    planningMode = "Selected period",
                                    plannedExpenses = plannedExpenses
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Plan")
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    millis: Long,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedTextField(
        value = dateFormat.format(Date(millis)),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            TextButton(onClick = onClick) { Text("Change") }
        },
        modifier = modifier
    )
}

private fun showDatePicker(
    context: android.content.Context,
    selectedMillis: Long,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day, 12, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            onSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
private fun PlanResults(
    plan: TermPlan,
    expenses: List<Expense>,
    incomes: List<Income>,
    onEdit: () -> Unit,
    modifier: Modifier
) {
    var period by remember { mutableStateOf(PlanPeriod.TERM) }
    var customStart by remember { mutableStateOf(plan.startDateMillis) }
    var customEnd by remember { mutableStateOf(plan.endDateMillis) }
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val requestedRange = when (period) {
        PlanPeriod.TERM -> DateRange(startOfDay(plan.startDateMillis), endOfDay(plan.endDateMillis))
        PlanPeriod.SEVEN_DAYS -> trackingRange(TrackingPeriod.SEVEN_DAYS)
        PlanPeriod.ONE_MONTH -> trackingRange(TrackingPeriod.ONE_MONTH)
        PlanPeriod.THREE_MONTHS -> trackingRange(TrackingPeriod.THREE_MONTHS)
        PlanPeriod.CUSTOM -> DateRange(startOfDay(customStart), endOfDay(customEnd))
    }

    val analysis: PlanPeriodAnalysis = calculatePlanAnalysis(
        plan = plan,
        expenses = expenses,
        incomes = incomes,
        requestedRange = requestedRange
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Mode") },
                actions = { TextButton(onClick = onEdit) { Text("Edit") } }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                dateFormat.format(Date(plan.startDateMillis)) +
                    " → " +
                    dateFormat.format(Date(plan.endDateMillis)),
                style = MaterialTheme.typography.bodyMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = period == PlanPeriod.TERM,
                        onClick = { period = PlanPeriod.TERM },
                        label = { Text("Term") }
                    )
                    FilterChip(
                        selected = period == PlanPeriod.SEVEN_DAYS,
                        onClick = { period = PlanPeriod.SEVEN_DAYS },
                        label = { Text("7 Days") }
                    )
                    FilterChip(
                        selected = period == PlanPeriod.ONE_MONTH,
                        onClick = { period = PlanPeriod.ONE_MONTH },
                        label = { Text("1 Month") }
                    )
                    FilterChip(
                        selected = period == PlanPeriod.THREE_MONTHS,
                        onClick = { period = PlanPeriod.THREE_MONTHS },
                        label = { Text("3 Months") }
                    )
                    FilterChip(
                        selected = period == PlanPeriod.CUSTOM,
                        onClick = { period = PlanPeriod.CUSTOM },
                        label = { Text("Custom") }
                    )
                }
                Text(
                    "Term uses the entire saved plan. Other periods show the selected window inside the plan when dates overlap.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (period == PlanPeriod.CUSTOM) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField(
                        "From",
                        customStart,
                        dateFormat,
                        { showDatePicker(context, customStart) { customStart = it } },
                        Modifier.weight(1f)
                    )
                    DateField(
                        "To",
                        customEnd,
                        dateFormat,
                        { showDatePicker(context, customEnd) { customEnd = it } },
                        Modifier.weight(1f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text("TermRunway Health", style = MaterialTheme.typography.titleMedium)
                    Text(analysis.healthPercent.toString() + "%", style = MaterialTheme.typography.displaySmall)
                    Text(
                        if (analysis.positionGapCents >= 0L)
                            "Actual position is ₹" + ExpenseAmount.format(analysis.positionGapCents) + " above the plan."
                        else
                            "Actual position is ₹" + ExpenseAmount.format(-analysis.positionGapCents) + " below the plan."
                    )
                }
            }

            MetricCard("Healthy spending reference", "₹" + ExpenseAmount.format(analysis.healthyDailySpendingCents) + " / day")
            MetricCard("Planned spending for period", "₹" + ExpenseAmount.format(analysis.plannedSpendingForPeriodCents))
            MetricCard("Actual spending for period", "₹" + ExpenseAmount.format(analysis.actualSpendingForPeriodCents))
            MetricCard(
                "Spending difference",
                (if (analysis.spendingVarianceCents >= 0L) "+" else "-") +
                    "₹" + ExpenseAmount.format(kotlin.math.abs(analysis.spendingVarianceCents))
            )
            MetricCard("Actual income in period", "₹" + ExpenseAmount.format(analysis.actualIncomeForPeriodCents))
            MetricCard("Planned position", "₹" + ExpenseAmount.format(analysis.plannedPositionCents))
            MetricCard("Actual position", "₹" + ExpenseAmount.format(analysis.actualPositionCents))

            analysis.topCategoryVariance?.let { variance ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Largest category difference", style = MaterialTheme.typography.titleMedium)
                        Text(variance.category)
                        Text("Planned: ₹" + ExpenseAmount.format(variance.plannedCents))
                        Text("Actual: ₹" + ExpenseAmount.format(variance.actualCents))
                        Text(
                            if (variance.varianceCents >= 0L)
                                "Above plan by ₹" + ExpenseAmount.format(variance.varianceCents)
                            else
                                "Below plan by ₹" + ExpenseAmount.format(-variance.varianceCents)
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product rule", style = MaterialTheme.typography.titleMedium)
                    Text("The saved plan is the benchmark. Actual transactions change the analysis and suggestions, not the saved plan itself.")
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            val termRange = DateRange(startOfDay(plan.startDateMillis), endOfDay(plan.endDateMillis))
            val termAnalysis = calculatePlanAnalysis(plan, expenses, incomes, termRange)
            Text(
                "Whole-plan position: planned ₹" +
                    formatRupees(termAnalysis.plannedPositionCents) +
                    " • actual ₹" +
                    formatRupees(termAnalysis.actualPositionCents),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private enum class PlanPeriod {
    TERM,
    SEVEN_DAYS,
    ONE_MONTH,
    THREE_MONTHS,
    CUSTOM
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
