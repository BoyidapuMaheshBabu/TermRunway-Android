@file:OptIn(ExperimentalMaterial3Api::class)
package com.termrunway.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.Income
import com.termrunway.app.logic.DateRange
import com.termrunway.app.logic.TrackingPeriod
import com.termrunway.app.logic.calculateTrackingSummary
import com.termrunway.app.logic.trackingInsights
import com.termrunway.app.logic.trackingRange
import java.util.Calendar

@Composable
fun InsightsScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(TrackingPeriod.ONE_MONTH) }
    // Hardcoded custom range for now. In a real app we'd have a date picker.
    val customRange = remember {
        val today = Calendar.getInstance().timeInMillis
        DateRange(today - 14L * 24 * 60 * 60 * 1000, today)
    }

    val range = trackingRange(period, customRange = customRange)
    val summary = calculateTrackingSummary(incomes, expenses, range)
    val insights = trackingInsights(summary)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tracking Dashboard") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Period", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = period == TrackingPeriod.SEVEN_DAYS,
                        onClick = { period = TrackingPeriod.SEVEN_DAYS },
                        label = { Text("7 Days") }
                    )
                    FilterChip(
                        selected = period == TrackingPeriod.ONE_MONTH,
                        onClick = { period = TrackingPeriod.ONE_MONTH },
                        label = { Text("1 Month") }
                    )
                    FilterChip(
                        selected = period == TrackingPeriod.THREE_MONTHS,
                        onClick = { period = TrackingPeriod.THREE_MONTHS },
                        label = { Text("3 Months") }
                    )
                    FilterChip(
                        selected = period == TrackingPeriod.CUSTOM,
                        onClick = { period = TrackingPeriod.CUSTOM },
                        label = { Text("Custom") }
                    )
                }
            }

            if (summary.transactionCount == 0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No activity in this period", style = MaterialTheme.typography.titleMedium)
                        Text("Record an income or expense to see your tracking analysis.")
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Period summary", style = MaterialTheme.typography.titleMedium)
                        Text("Total Income: ₹" + ExpenseAmount.format(summary.totalIncomeCents))
                        Text("Total Expense: ₹" + ExpenseAmount.format(summary.totalExpenseCents))
                        Text("Net Change: ₹" + ExpenseAmount.format(summary.netChangeCents))
                        Text("Average Daily Expense: ₹" + ExpenseAmount.format(summary.averageDailyExpenseCents))
                        Text("Transactions: ${summary.transactionCount}")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Observations", style = MaterialTheme.typography.titleMedium)
                        if (insights.isEmpty()) {
                            Text("No observations available.")
                        } else {
                            insights.forEach { insight ->
                                Text("• $insight")
                            }
                        }
                    }
                }

                if (summary.categoryTotalsCents.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Categories", style = MaterialTheme.typography.titleMedium)
                            summary.categoryTotalsCents.forEach { (category, amount) ->
                                val pct = summary.categoryPercentages[category] ?: 0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(category)
                                    Text("₹${ExpenseAmount.format(amount)} ($pct%)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
