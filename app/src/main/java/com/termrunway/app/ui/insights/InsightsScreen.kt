package com.termrunway.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.ui.daily.categoryIcon
import com.termrunway.app.ui.daily.formatLongDate
import com.termrunway.app.ui.daily.formatRupees
import com.termrunway.app.ui.daily.formatSignedRupees
import com.termrunway.app.ui.daily.startOfDay

@Composable
fun InsightsScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rangeDays by rememberSaveable { mutableStateOf(7) }
    val today = startOfDay(System.currentTimeMillis())
    val from = startOfDay(System.currentTimeMillis()) - (rangeDays - 1).toLong() * 86_400_000L
    val filteredExpenses = expenses.filter { startOfDay(it.dateMillis) in from..today }
    val filteredIncome = incomes.filter { startOfDay(it.dateMillis) in from..today }
    val spent = filteredExpenses.sumOf { it.amountCents }
    val income = filteredIncome.sumOf { it.amountCents }
    val spendingDays = filteredExpenses.map { startOfDay(it.dateMillis) }.distinct().size
    val average = if (spendingDays == 0) 0L else spent / spendingDays
    val categoryTotals = filteredExpenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amountCents } }
        .toList().sortedByDescending { it.second }
    val highest = filteredExpenses.groupBy { startOfDay(it.dateMillis) }
        .mapValues { entry -> entry.value.sumOf { it.amountCents } }
        .maxByOrNull { it.value }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Understand your spending", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettings) { Icon(Icons.Outlined.Wallet, "Settings") }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(7, 30, 90).forEachIndexed { index, days ->
                    SegmentedButton(
                        selected = rangeDays == days,
                        onClick = { rangeDays = days },
                        shape = SegmentedButtonDefaults.itemShape(index, 3)
                    ) { Text(days.toString() + "d") }
                }
            }
        }

        if (filteredExpenses.isEmpty() && filteredIncome.isEmpty()) {
            item {
                Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Not enough data yet", fontWeight = FontWeight.SemiBold)
                        Text("Keep recording money. Useful patterns will appear here once there is enough activity.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Metric("Spent", formatRupees(spent), Modifier.weight(1f))
                    Metric("Income", formatRupees(income), Modifier.weight(1f))
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Metric("Net", formatSignedRupees(income - spent), Modifier.weight(1f))
                    Metric("Avg active day", formatRupees(average), Modifier.weight(1f))
                }
            }

            if (highest != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ShowChart, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Highest spending day", fontWeight = FontWeight.SemiBold)
                                Text(formatLongDate(highest.key) + " · " + formatRupees(highest.value))
                            }
                        }
                    }
                }
            }

            item { Text("Spending by category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(categoryTotals.take(8), key = { it.first }) { entry ->
                CategoryRow(entry.first, entry.second, spent)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryRow(category: String, amount: Long, total: Long) {
    Card(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(categoryIcon(category), null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(category, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text(formatRupees(amount), fontWeight = FontWeight.SemiBold)
            }
            val fraction = if (total <= 0) 0f else amount.toFloat() / total.toFloat()
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth(fraction).height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
