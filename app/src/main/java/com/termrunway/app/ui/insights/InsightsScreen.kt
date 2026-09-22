package com.termrunway.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.Income
import com.termrunway.app.logic.DateRange
import com.termrunway.app.logic.TrackingPeriod
import com.termrunway.app.logic.calculateTrackingSummary
import com.termrunway.app.logic.trackingInsight
import com.termrunway.app.logic.trackingRange

@Composable
fun InsightsScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(TrackingPeriod.ONE_MONTH) }
    val range = trackingRange(period)
    val summary = calculateTrackingSummary(incomes, expenses, range)
    val days = com.termrunway.app.logic.daysBetweenInclusive(range.startMillis, range.endMillis)

    val previousRange = DateRange(
        startMillis = range.startMillis - (days * com.termrunway.app.logic.DAY_MILLIS),
        endMillis = range.startMillis - com.termrunway.app.logic.DAY_MILLIS
    )
    val previous = calculateTrackingSummary(incomes, expenses, previousRange)

    val insight = trackingInsight(summary, previous)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Insights") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Actual spending insights", style = MaterialTheme.typography.titleLarge)
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
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Period summary", style = MaterialTheme.typography.titleMedium)
                    Text("Income: ₹" + ExpenseAmount.format(summary.totalIncomeCents))
                    Text("Spending: ₹" + ExpenseAmount.format(summary.totalExpenseCents))
                    Text("Net change: ₹" + ExpenseAmount.format(summary.netChangeCents))
                    Text("Average spending: ₹" + ExpenseAmount.format(summary.averageDailyExpenseCents) + " / day")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("What the data says", style = MaterialTheme.typography.titleMedium)
                    Text(insight ?: "Record a few expenses in this period to generate an insight.")
                }
            }

            if (summary.categoryTotalsCents.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Category spending", style = MaterialTheme.typography.titleMedium)
                        summary.categoryTotalsCents.forEach { (category, amount) ->
                            Text(category + ": ₹" + ExpenseAmount.format(amount))
                        }
                    }
                }
            }
        }
    }
}
