package com.termrunway.app.ui.screens.insights

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
import com.termrunway.app.ui.components.*
import com.termrunway.app.ui.util.addDays
import com.termrunway.app.ui.util.startOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: AppUiState,
    onSettings: () -> Unit
) {
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
            ScreenHeader(
                title = "Insights",
                subtitle = "Understand your real spending",
                onSettings = onSettings
            )
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
                    MoneyPulseChart(
                        transactions = state.transactions,
                        startMs = start,
                        windowDays = window
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
            item { SectionTitle("Plan view") }
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
    windowDays: Int
) {
    val rawDays = windowDays.coerceIn(7, 90)
    val points = if (rawDays == 7) {
        (0 until rawDays).map { offset ->
            val day = addDays(startMs, offset)
            MoneyPulsePoint(
                label = if (offset == rawDays - 1) "Today" else java.text.SimpleDateFormat("dd MMM", Locale.getDefault()).format(java.util.Date(day)),
                incomePaise = FinancialCalculator.dayIncome(transactions, day),
                expensePaise = FinancialCalculator.dayExpense(transactions, day)
            )
        }
    } else {
        val weeks = (rawDays + 6) / 7
        (0 until weeks).map { week ->
            val weekStart = addDays(startMs, week * 7)
            val length = minOf(7, rawDays - week * 7)
            MoneyPulsePoint(
                label = java.text.SimpleDateFormat("dd MMM", Locale.getDefault()).format(java.util.Date(weekStart)),
                incomePaise = (0 until length).sumOf { offset ->
                    FinancialCalculator.dayIncome(transactions, addDays(weekStart, offset))
                },
                expensePaise = (0 until length).sumOf { offset ->
                    FinancialCalculator.dayExpense(transactions, addDays(weekStart, offset))
                }
            )
        }
    }

    val totalIncomePaise = points.sumOf { it.incomePaise }
    val totalExpensePaise = points.sumOf { it.expensePaise }
    val highestPaise = points.maxOfOrNull { maxOf(it.incomePaise, it.expensePaise) } ?: 0L
    val highestPoint = points.maxByOrNull { maxOf(it.incomePaise, it.expensePaise) }
    val averageIncomePaise = totalIncomePaise / rawDays.toLong().coerceAtLeast(1L)
    val averageExpensePaise = totalExpensePaise / rawDays.toLong().coerceAtLeast(1L)
    val chartMaxPaise = highestPaise.coerceAtLeast(1L)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            if (rawDays == 7) "Actual cash flow · each pair is one day" else "Actual cash flow · grouped by week for readability",
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
private fun MoneyPulseLegend(label: String, indicator: androidx.compose.ui.graphics.Color) {
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
    val rupees = kotlin.math.abs(paise) / 100.0
    val sign = if (paise < 0) "-" else ""
    return when {
        rupees >= 100000 -> sign + "₹" + String.format(Locale.getDefault(), "%.1fL", rupees / 100000.0)
        rupees >= 1000 -> sign + "₹" + String.format(Locale.getDefault(), "%.1fk", rupees / 1000.0)
        else -> sign + "₹" + String.format(Locale.getDefault(), "%.0f", rupees)
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

