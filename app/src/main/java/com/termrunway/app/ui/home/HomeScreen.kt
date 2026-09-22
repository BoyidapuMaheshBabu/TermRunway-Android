@file:OptIn(ExperimentalMaterial3Api::class)
package com.termrunway.app.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.Income
import com.termrunway.app.logic.DateRange
import com.termrunway.app.logic.TrackingPeriod
import com.termrunway.app.logic.calculateTrackingSummary
import com.termrunway.app.logic.trackingRange
import java.util.Calendar

@Composable
fun HomeScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    onOpenActivity: () -> Unit,
    onOpenInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(TrackingPeriod.ONE_MONTH) }
    val customRange = remember {
        val today = Calendar.getInstance().timeInMillis
        DateRange(today - 14L * 24 * 60 * 60 * 1000, today)
    }

    val range = trackingRange(period, customRange = customRange)
    val tracking = calculateTrackingSummary(incomes, expenses, range)

    val totalRecordedIncome = incomes.sumOf { it.amountCents }
    val totalRecordedExpense = expenses.sumOf { it.amountCents }
    val currentRecordedBalance = totalRecordedIncome - totalRecordedExpense

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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Overall Recorded Balance", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Based on all recorded transactions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("₹" + ExpenseAmount.format(currentRecordedBalance), style = MaterialTheme.typography.headlineMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Period Analysis", style = MaterialTheme.typography.titleMedium)
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Income: ₹" + ExpenseAmount.format(tracking.totalIncomeCents))
                    Text("Expenses: ₹" + ExpenseAmount.format(tracking.totalExpenseCents))
                    Text("Net Change: ₹" + ExpenseAmount.format(tracking.netChangeCents))
                    Text("Average Daily Expense: ₹" + ExpenseAmount.format(tracking.averageDailyExpenseCents))
                    Text("Transactions: ${tracking.transactionCount}")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onOpenInsights, modifier = Modifier.weight(1f)) {
                    Text("Insights")
                }
                Button(onClick = onOpenActivity, modifier = Modifier.weight(1f)) {
                    Text("Activity")
                }
            }
        }
    }
}
