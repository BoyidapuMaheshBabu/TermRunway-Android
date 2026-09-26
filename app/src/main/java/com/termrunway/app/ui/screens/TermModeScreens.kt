package com.termrunway.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.data.ExpectedIncome
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.TermPlan
import com.termrunway.app.domain.TermStatus
import com.termrunway.app.domain.categoryComparisons
import com.termrunway.app.domain.parseMoneyToCents
import com.termrunway.app.domain.termSummary
import com.termrunway.app.ui.components.ModeSwitcher
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.MetricCard
import com.termrunway.app.ui.navigation.TermTab
import com.termrunway.app.util.addDays
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.formatRupees
import com.termrunway.app.util.formatSignedRupees
import com.termrunway.app.util.startOfDay
import java.util.Calendar
import java.util.UUID
import kotlin.math.max

@Composable
fun TermOverviewScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onOpenTab: (TermTab) -> Unit
) {
    val plan = data.termPlan
    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TERM RUNWAY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("PLAN • CALCULATE • ACHIEVE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }
                TextButton(onClick = onSettings) { Text("Settings") }
            }
        }
        item { ModeSwitcher(planSelected = true, onSelectPlan = onModeChange) }

        if (plan == null) {
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Build your first plan", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Tell TermRunway how much you have, what income you expect, and what you expect to spend. The app will calculate the consequences while you plan.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { onOpenTab(TermTab.PLAN) }) { Text("Create plan") }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Actual money still comes from Tracking", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Plan Tracking never creates fake transactions. Your real income and expenses continue to come from the same offline transaction system.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val summary = termSummary(data, plan)
            item {
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("RUNWAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    formatRupees(summary.currentBalanceCents),
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Text("current balance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(summary.status)
                        }
                        LinearProgressIndicator(progress = { summary.progressRatio }, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard("Days left", summary.daysRemaining.toString(), Modifier.weight(1f))
                            MetricCard("Safe / day", formatRupees(summary.safeDailyPaceCents), Modifier.weight(1f))
                        }
                        Text(
                            "Projected end: " + formatRupees(summary.projectedEndBalanceCents),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Planned", formatRupees(summary.plannedExpenseCents), Modifier.weight(1f))
                    MetricCard("Actual", formatRupees(summary.actualExpenseCents), Modifier.weight(1f))
                    MetricCard("Plan gap", formatSignedRupees(-summary.varianceToPlanCents), Modifier.weight(1f))
                }
            }

            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Term window", fontWeight = FontWeight.SemiBold)
                        Text(formatDay(plan.startDateMillis) + " → " + formatDay(plan.endDateMillis))
                        Text(
                            summary.totalDays.toString() + " days · " +
                                formatRupees(plan.availableMoneyCents) + " available now · " +
                                formatRupees(summary.expectedIncomeCents) + " expected income",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onOpenTab(TermTab.PLAN) }, modifier = Modifier.weight(1f)) {
                        Text("Edit plan")
                    }
                    Button(onClick = { onOpenTab(TermTab.RUNWAY) }, modifier = Modifier.weight(1f)) {
                        Text("Open runway")
                    }
                }
            }
        }
    }
}

@Composable
fun TermPlanScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onSave: (TermPlan) -> Unit
) {
    val existing = data.termPlan
    val today = startOfDay(System.currentTimeMillis())
    var startDate by remember(existing) { mutableLongStateOf(existing?.startDateMillis ?: today) }
    var endDate by remember(existing) { mutableLongStateOf(existing?.endDateMillis ?: addDays(today, 89)) }
    var availableText by remember(existing) {
        mutableStateOf(
            if (existing == null) "" else (existing.availableMoneyCents / 100.0).toString()
        )
    }
    val defaultIncomeSources = listOf("Parent support", "Part-time", "Scholarship", "Other")
    var incomeRows by remember(existing) {
        mutableStateOf(
            defaultIncomeSources.map { default ->
                val match = existing?.expectedIncome?.firstOrNull { it.source == default }
                Pair(default, if (match == null) "" else (match.amountCents / 100.0).toString())
            }
        )
    }
    var expenseTexts by remember(existing) {
        mutableStateOf(
            ExpenseCategories.ALL.associateWith { category ->
                val amount = existing?.plannedExpenses?.firstOrNull { it.category == category }?.amountCents ?: 0L
                if (amount == 0L) "" else (amount / 100.0).toString()
            }
        )
    }
    var errorMessage by remember(existing) { mutableStateOf<String?>(null) }

    val availableCents = parseMoneyToCents(availableText) ?: 0L
    val incomeTotal = incomeRows.sumOf { parseMoneyToCents(it.second) ?: 0L }
    val expenseTotal = expenseTexts.values.sumOf { parseMoneyToCents(it) ?: 0L }
    val totalFunds = availableCents + incomeTotal
    val projected = totalFunds - expenseTotal
    val days = ((startOfDay(endDate) - startOfDay(startDate)) / DAY_MILLIS).toInt() + 1

    val context = LocalContext.current

    fun pickDate(current: Long, onPicked: (Long) -> Unit) {
        val initial = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onPicked(startOfDay(chosen))
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Plan", style = MaterialTheme.typography.headlineSmall)
                        Text("Shape the term before you live it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onSettings) { Text("Settings") }
                }
            }
        }
        item { ModeSwitcher(planSelected = true, onSelectPlan = onModeChange) }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live plan math", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                    Text(formatRupees(availableCents), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("available now", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard("Expected income", formatRupees(incomeTotal), Modifier.weight(1f))
                        MetricCard("Planned expense", formatRupees(expenseTotal), Modifier.weight(1f))
                    }
                    Text(
                        "Projected left: " + formatSignedRupees(projected),
                        fontWeight = FontWeight.SemiBold,
                        color = if (projected < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (projected < 0L) {
                        Text(
                            "This plan is not sufficient yet. Reduce a category or increase expected income.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (expenseTotal > 0L && days > 0) {
                        Text(
                            "Planned pace: " + formatRupees(expenseTotal / days) + " per day over " + days + " days.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item { Text("Term dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickDate(startDate) { startDate = it } }, modifier = Modifier.weight(1f)) {
                    Text("From\n" + formatDay(startDate))
                }
                OutlinedButton(onClick = { pickDate(endDate) { endDate = it } }, modifier = Modifier.weight(1f)) {
                    Text("Until\n" + formatDay(endDate))
                }
            }
        }
        if (days <= 0) {
            item {
                Text("Choose an end date on or after the start date.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            item {
                Text(
                    days.toString() + " days in this plan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item { Text("Money available now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            OutlinedTextField(
                value = availableText,
                onValueChange = { availableText = sanitizeMoney(it); errorMessage = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current available money (₹)") },
                singleLine = true
            )
        }

        item { Text("Expected income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                incomeRows.forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = row.first,
                            onValueChange = {
                                val updated = incomeRows.toMutableList()
                                updated[index] = Pair(it.take(40), row.second)
                                incomeRows = updated
                            },
                            modifier = Modifier.weight(1.2f),
                            label = { Text("Source") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = row.second,
                            onValueChange = {
                                val updated = incomeRows.toMutableList()
                                updated[index] = Pair(row.first, sanitizeMoney(it))
                                incomeRows = updated
                            },
                            modifier = Modifier.weight(0.8f),
                            label = { Text("₹") },
                            singleLine = true
                        )
                    }
                }
            }
        }

        item { Text("Expected expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(ExpenseCategories.ALL, key = { it }) { category ->
            OutlinedTextField(
                value = expenseTexts[category].orEmpty(),
                onValueChange = { value ->
                    expenseTexts = expenseTexts.toMutableMap().apply { put(category, sanitizeMoney(value)) }
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(category) },
                singleLine = true
            )
        }

        errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }

        item {
            Button(
                enabled = days > 0,
                onClick = {
                    val invalidIncome = incomeRows.any { it.second.isNotBlank() && parseMoneyToCents(it.second) == null }
                    val invalidExpenses = expenseTexts.values.any { it.isNotBlank() && parseMoneyToCents(it) == null }
                    when {
                        availableText.isNotBlank() && parseMoneyToCents(availableText) == null -> errorMessage = "Enter a valid available amount."
                        invalidIncome -> errorMessage = "Check the expected income amounts."
                        invalidExpenses -> errorMessage = "Check the planned expense amounts."
                        else -> {
                            val plan = TermPlan(
                                startDateMillis = startOfDay(startDate),
                                endDateMillis = startOfDay(endDate),
                                availableMoneyCents = availableCents,
                                expectedIncome = incomeRows
                                    .filter { it.first.trim().isNotEmpty() && (parseMoneyToCents(it.second) ?: 0L) > 0L }
                                    .map {
                                        ExpectedIncome(
                                            id = UUID.randomUUID().toString(),
                                            source = it.first.trim(),
                                            amountCents = parseMoneyToCents(it.second) ?: 0L
                                        )
                                    },
                                plannedExpenses = expenseTexts
                                    .mapNotNull { entry ->
                                        val amount = parseMoneyToCents(entry.value) ?: 0L
                                        if (amount > 0L) PlannedExpense(entry.key, amount) else null
                                    }
                            )
                            onSave(plan)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing == null) "Save term plan" else "Update term plan")
            }
        }

        item {
            Text(
                "Everything in Plan Tracking stays on this device. Actual money still comes from your transaction history.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TermRunwayScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onModeChange: (Boolean) -> Unit
) {
    val plan = data.termPlan
    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Runway", style = MaterialTheme.typography.headlineSmall)
                    Text("What your current pace means", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onSettings) { Text("Settings") }
            }
        }
        item { ModeSwitcher(planSelected = true, onSelectPlan = onModeChange) }

        if (plan == null) {
            item {
                EmptyState(
                    title = "No term plan yet",
                    description = "Create a plan first and Runway will turn it into a live time-and-money forecast."
                )
            }
        } else {
            val summary = termSummary(data, plan)
            item {
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("RUNWAY", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(formatRupees(summary.currentBalanceCents), style = MaterialTheme.typography.displaySmall)
                        Text("current balance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress = { summary.progressRatio }, modifier = Modifier.fillMaxWidth())
                        Text(
                            summary.daysRemaining.toString() + " days remaining",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Safe / day", formatRupees(summary.safeDailyPaceCents), Modifier.weight(1f))
                    MetricCard("Planned / day", formatRupees(summary.plannedDailyPaceCents), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Actual / day", formatRupees(summary.actualDailyPaceCents), Modifier.weight(1f))
                    MetricCard("Projected end", formatSignedRupees(summary.projectedEndBalanceCents), Modifier.weight(1f))
                }
            }
            item {
                val outlook = when (summary.status) {
                    TermStatus.HEALTHY -> "Your current pace is inside the plan."
                    TermStatus.ABOVE_PLAN -> "Your spending is running ahead of the plan."
                    TermStatus.INSUFFICIENT -> "The current plan does not fully cover expected spending."
                    TermStatus.NO_PLAN -> "Create a plan first."
                }
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (summary.status) {
                            TermStatus.INSUFFICIENT -> MaterialTheme.colorScheme.errorContainer
                            TermStatus.ABOVE_PLAN -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Runway outlook", fontWeight = FontWeight.SemiBold)
                        Text(outlook)
                        Text(
                            "Remaining expected income: " + formatRupees(summary.remainingExpectedIncomeCents) +
                                " · remaining planned expense: " + formatRupees(summary.remainingPlannedExpenseCents),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermInsightsScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onModeChange: (Boolean) -> Unit
) {
    val plan = data.termPlan
    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Term Insights", style = MaterialTheme.typography.headlineSmall)
                    Text("Plan versus reality", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onSettings) { Text("Settings") }
            }
        }
        item { ModeSwitcher(planSelected = true, onSelectPlan = onModeChange) }

        if (plan == null) {
            item {
                EmptyState(
                    title = "Plan insights need a plan",
                    description = "Create a term plan and your real transaction history will be compared against it."
                )
            }
        } else {
            val summary = termSummary(data, plan)
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("Expected income", formatRupees(summary.expectedIncomeCents), Modifier.weight(1f))
                    MetricCard("Actual income", formatRupees(summary.actualIncomeCents), Modifier.weight(1f))
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Plan vs actual", fontWeight = FontWeight.SemiBold)
                        Text(
                            "A positive variance means actual expense is above the plan-to-date pace.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            val comparisons = categoryComparisons(data, plan)
            items(comparisons, key = { it.category }) { comparison ->
                val max = max(comparison.plannedCents, comparison.actualCents).coerceAtLeast(1L)
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(comparison.category, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(formatRupees(comparison.actualCents))
                        }
                        LinearProgressIndicator(
                            progress = { (comparison.actualCents.toDouble() / max.toDouble()).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plan " + formatRupees(comparison.plannedCents), style = MaterialTheme.typography.labelMedium)
                            Text(
                                if (comparison.varianceCents <= 0L) {
                                    formatRupees(-comparison.varianceCents) + " under"
                                } else {
                                    formatRupees(comparison.varianceCents) + " over"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (comparison.varianceCents > 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TermStatus) {
    val label = when (status) {
        TermStatus.HEALTHY -> "On track"
        TermStatus.ABOVE_PLAN -> "Above plan"
        TermStatus.INSUFFICIENT -> "Plan needs work"
        TermStatus.NO_PLAN -> "No plan"
    }
    FilterChip(selected = false, onClick = {}, label = { Text(label) })
}

private fun sanitizeMoney(value: String): String =
    value.filter { it.isDigit() || it == '.' }.take(14)

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
