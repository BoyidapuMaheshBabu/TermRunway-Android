package com.termrunway.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.ScreenHeader
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.components.moneyString
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.util.PeriodPreset
import com.termrunway.app.ui.util.addDays
import com.termrunway.app.ui.util.endOfDay
import com.termrunway.app.ui.util.pickDate
import com.termrunway.app.ui.util.resolvePeriodRange
import com.termrunway.app.ui.util.startOfDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun planStatusSentence(metrics: PlanMetrics): String {
    val status = metrics.status
    val variance = metrics.spendVariancePaise
    val expected = metrics.expectedSpendToDatePaise
    val actual = metrics.actualExpensePaise

    if (status == "Upcoming") {
        return "Your plan hasn't started yet. Your planned income and expenses are ready for the period."
    }
    if (status == "Completed") {
        return "Your plan has ended. Review how your actual spending compared with your plan."
    }
    if (status == "Overdrawn" || (metrics.actualRemainingPaise < 0)) {
        return "Your current spending is higher than the money available in this plan."
    }

    if (expected == 0L) {
        return if (actual == 0L) {
            "No spending has been recorded yet, and none was expected by this point in the plan."
        } else {
            "No spending was expected by this point in your plan, but ${moneyString(actual)} has been recorded."
        }
    }

    val ratio = actual.toDouble() / expected.toDouble()

    return when {
        abs(variance) < 20_00 -> "Your spending is currently on track with your plan."
        ratio in 0.95..1.05 -> "Your spending is currently on track with your plan."
        ratio in 1.05..1.25 -> "Your spending is slightly above the pace expected by your plan."
        ratio in 1.25..2.0 -> "Your spending is noticeably above the pace expected by your plan."
        ratio in 2.0..3.0 -> "Your spending is much faster than the pace expected by your plan."
        ratio > 3.0 -> "Your spending is far above the pace expected by your plan."
        ratio in 0.75..<0.95 -> "Your spending is slightly below the pace expected by your plan."
        ratio in 0.40..<0.75 -> "Your spending is noticeably below the pace expected by your plan."
        else -> "Your spending is well below the pace expected by your plan."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: AppUiState,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    var selectedPreset by rememberSaveable { mutableStateOf(PeriodPreset.DAYS_7) }
    var customStartMs by rememberSaveable { mutableLongStateOf(addDays(System.currentTimeMillis(), -30)) }
    var customEndMs by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    val periodChoices = remember(state.activePlan) {
        buildList {
            add(PeriodPreset.DAYS_7)
            add(PeriodPreset.DAYS_30)
            add(PeriodPreset.DAYS_90)
            if (state.activePlan != null) {
                add(PeriodPreset.ACTIVE_PLAN)
            }
            add(PeriodPreset.ALL_TIME)
            add(PeriodPreset.CUSTOM)
        }
    }

    val resolvedRange = remember(selectedPreset, state.activePlan, customStartMs, customEndMs) {
        resolvePeriodRange(selectedPreset, state.activePlan, customStartMs, customEndMs)
    }

    val earliestTxMs = state.transactions.minOfOrNull { it.dateMs }
    val defaultStartMs = earliestTxMs?.let { startOfDay(it) } ?: startOfDay(addDays(System.currentTimeMillis(), -30))

    val startMs = resolvedRange.startMs ?: defaultStartMs
    val endMs = resolvedRange.endMs ?: endOfDay(System.currentTimeMillis())

    val income = FinancialCalculator.rangeIncome(state.transactions, startMs, endMs)
    val expense = FinancialCalculator.rangeExpense(state.transactions, startMs, endMs)
    val categories = FinancialCalculator.categoryTotals(
        state.transactions,
        TransactionType.EXPENSE,
        startMs,
        endMs
    )
    val maxCategory = categories.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "Insights",
                subtitle = "Understand your real spending",
                onSettings = onSettings
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periodChoices.forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { selectedPreset = preset },
                        label = { Text(preset.label) }
                    )
                }
            }
        }
        if (selectedPreset == PeriodPreset.CUSTOM) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pickDate(context, customStartMs) { customStartMs = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text("From: " + dateLabel(customStartMs), maxLines = 1) }
                    OutlinedButton(
                        onClick = { pickDate(context, customEndMs) { customEndMs = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text("To: " + dateLabel(customEndMs), maxLines = 1) }
                }
            }
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
                    MoneyPulseChart(
                        transactions = state.transactions,
                        startMs = startMs,
                        endMs = endMs
                    )
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
            item { SectionTitle("How your plan is going") }
            item { PlanComparePanel(metrics) }
        }
    }
}

private data class MoneyPulsePoint(
    val label: String,
    val incomePaise: Long,
    val expensePaise: Long
)

@Composable
private fun MoneyPulseChart(
    transactions: List<Transaction>,
    startMs: Long,
    endMs: Long
) {
    val totalDays = FinancialCalculator.daysInclusive(startMs, endMs).coerceAtLeast(1)
    val points = remember(transactions, startMs, endMs, totalDays) {
        if (totalDays <= 14) {
            (0 until totalDays).map { offset ->
                val day = addDays(startMs, offset)
                MoneyPulsePoint(
                    label = if (FinancialCalculator.sameDay(day, System.currentTimeMillis())) "Today" else SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(day)),
                    incomePaise = FinancialCalculator.dayIncome(transactions, day),
                    expensePaise = FinancialCalculator.dayExpense(transactions, day)
                )
            }
        } else if (totalDays <= 90) {
            val weeks = (totalDays + 6) / 7
            (0 until weeks).map { week ->
                val weekStart = addDays(startMs, week * 7)
                val length = minOf(7, totalDays - week * 7)
                MoneyPulsePoint(
                    label = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(weekStart)),
                    incomePaise = (0 until length).sumOf { offset ->
                        FinancialCalculator.dayIncome(transactions, addDays(weekStart, offset))
                    },
                    expensePaise = (0 until length).sumOf { offset ->
                        FinancialCalculator.dayExpense(transactions, addDays(weekStart, offset))
                    }
                )
            }
        } else {
            val cal = Calendar.getInstance().apply { timeInMillis = startMs }
            val endCal = Calendar.getInstance().apply { timeInMillis = endMs }
            val pointsList = mutableListOf<MoneyPulsePoint>()
            while (cal.timeInMillis <= endCal.timeInMillis || FinancialCalculator.sameDay(cal.timeInMillis, endCal.timeInMillis)) {
                val monthStart = cal.timeInMillis
                val monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(monthStart))
                var monthInc = 0L
                var monthExp = 0L
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)
                while (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear && cal.timeInMillis <= endCal.timeInMillis) {
                    monthInc += FinancialCalculator.dayIncome(transactions, cal.timeInMillis)
                    monthExp += FinancialCalculator.dayExpense(transactions, cal.timeInMillis)
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                pointsList.add(MoneyPulsePoint(monthLabel, monthInc, monthExp))
            }
            pointsList
        }
    }

    val totalIncomePaise = points.sumOf { it.incomePaise }
    val totalExpensePaise = points.sumOf { it.expensePaise }
    val highestPaise = points.maxOfOrNull { maxOf(it.incomePaise, it.expensePaise) } ?: 0L
    val highestPoint = points.maxByOrNull { maxOf(it.incomePaise, it.expensePaise) }
    val averageIncomePaise = totalIncomePaise / totalDays.toLong().coerceAtLeast(1L)
    val averageExpensePaise = totalExpensePaise / totalDays.toLong().coerceAtLeast(1L)
    val chartMaxPaise = highestPaise.coerceAtLeast(1L)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            when {
                totalDays <= 14 -> "Actual cash flow · each pair is one day"
                totalDays <= 90 -> "Actual cash flow · grouped by week for readability"
                else -> "Actual cash flow · grouped by month for readability"
            },
            style = MaterialTheme.typography.bodySmall,
            color = RunwayMuted
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyPulseSummary("Income", totalIncomePaise, Modifier.weight(1f))
            MoneyPulseSummary("Spent", totalExpensePaise, Modifier.weight(1f))
            MoneyPulseSummary("Net", totalIncomePaise - totalExpensePaise, Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MoneyPulseLegend("Income", RunwayMint)
                MoneyPulseLegend("Expense", RunwayRed)
            }
        }

        if (totalIncomePaise == 0L && totalExpensePaise == 0L) {
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(Icons.Outlined.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("No money movement recorded", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Record income or an expense after it actually happens. Your cash-flow bars will appear here.",
                        color = RunwayMuted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(188.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        Modifier.width(50.dp).fillMaxHeight().padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(compactMoney(chartMaxPaise), style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                        Text(compactMoney(chartMaxPaise / 2L), style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                        Text("₹0", style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                    }

                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.fillMaxWidth().weight(1f)) {
                            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }

                            Row(
                                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (points.size <= 7) 8.dp else 4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                points.forEach { point ->
                                    val incomeFraction = (point.incomePaise.toDouble() / chartMaxPaise.toDouble()).toFloat().coerceIn(0f, 1f)
                                    val expenseFraction = (point.expensePaise.toDouble() / chartMaxPaise.toDouble()).toFloat().coerceIn(0f, 1f)
                                    Row(
                                        Modifier.weight(1f).fillMaxHeight(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        if (point.incomePaise > 0L) {
                                            Box(
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .padding(horizontal = 2.dp),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(maxOf(8f, 154f * incomeFraction).dp)
                                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                        .background(RunwayMint)
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }

                                        if (point.expensePaise > 0L) {
                                            Box(
                                                Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .padding(horizontal = 2.dp),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(maxOf(8f, 154f * expenseFraction).dp)
                                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                        .background(RunwayRed)
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth().padding(start = 6.dp, top = 7.dp, end = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (points.size <= 7) 8.dp else 4.dp)
                        ) {
                            points.forEach { point ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Text(point.label, style = MaterialTheme.typography.labelSmall, color = RunwayMuted, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                if (highestPoint != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        val highestType = if (highestPoint.incomePaise >= highestPoint.expensePaise) "Income" else "Expense"
                        Text(
                            "Highest $highestType: ${compactMoney(highestPaise)} · ${highestPoint.label}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text("Avg income ${compactMoney(averageIncomePaise)} / day", style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                    Text("Avg spent ${compactMoney(averageExpensePaise)} / day", style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                }
            }
        }
    }
}

@Composable
private fun MoneyPulseLegend(label: String, indicator: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            Modifier.size(9.dp).clip(CircleShape).background(indicator)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
    }
}

@Composable
private fun MoneyPulseSummary(
    label: String,
    amountPaise: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = RunwayMuted
            )
            Text(
                compactMoney(amountPaise),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private fun compactMoney(paise: Long): String {
    val rupees = abs(paise) / 100.0
    val sign = if (paise < 0) "-" else ""
    return when {
        rupees >= 100000 -> sign + "₹" + String.format(Locale.getDefault(), "%.1fL", rupees / 100000.0)
        rupees >= 1000 -> sign + "₹" + String.format(Locale.getDefault(), "%.1fk", rupees / 100.0)
        else -> sign + "₹" + String.format(Locale.getDefault(), "%.0f", rupees)
    }
}

@Composable
private fun PlanComparePanel(metrics: PlanMetrics) {
    val statusSentence = planStatusSentence(metrics)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            statusSentence,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Spending Pace Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Spending Pace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Expected spend so far", style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(moneyString(metrics.expectedSpendToDatePaise), style = MaterialTheme.typography.bodyLarge)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Actual spending", style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            moneyString(metrics.actualExpensePaise),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RunwayRed
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                val variance = metrics.spendVariancePaise
                val varianceText = when {
                    variance > 0 -> "${moneyString(variance)} above expected pace"
                    variance < 0 -> "${moneyString(abs(variance))} below expected pace"
                    else -> "Spending is on pace with the plan"
                }
                val varianceColor = when {
                    variance > 0 -> RunwayRed
                    else -> RunwayMint
                }

                Text(
                    varianceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = varianceColor
                )
            }
        }

        // Money Position Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Money Position", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Expected at plan end", style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(moneyString(metrics.expectedRemainingPaise), style = MaterialTheme.typography.bodyLarge)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Money remaining now", style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            moneyString(metrics.actualRemainingPaise),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                val diff = metrics.actualRemainingPaise - metrics.expectedRemainingPaise
                val diffText = if (diff == 0L) {
                    "On track for expected end balance"
                } else {
                    "${moneyString(abs(diff))} difference"
                }

                Text(
                    diffText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "Based on this plan and your actual transactions.",
            style = MaterialTheme.typography.labelSmall,
            color = RunwayMuted
        )
    }
}
