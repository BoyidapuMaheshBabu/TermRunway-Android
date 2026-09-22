package com.termrunway.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import com.termrunway.app.logic.DateRange
import com.termrunway.app.logic.TrackingPeriod
import com.termrunway.app.logic.calculatePlanAnalysis
import com.termrunway.app.logic.calculateTrackingSummary
import com.termrunway.app.logic.endOfDay
import com.termrunway.app.logic.startOfDay
import com.termrunway.app.logic.trackingRange

@Composable
fun HomeScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    plan: TermPlan?,
    onOpenPlan: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracking = calculateTrackingSummary(
        incomes = incomes,
        expenses = expenses,
        range = trackingRange(TrackingPeriod.SEVEN_DAYS)
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("TermRunway") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Daily Tracking", style = MaterialTheme.typography.titleLarge)
                    Text("Last 7 days")
                    Text("Income: ₹" + ExpenseAmount.format(tracking.totalIncomeCents))
                    Text("Spending: ₹" + ExpenseAmount.format(tracking.totalExpenseCents))
                    Text("Average: ₹" + ExpenseAmount.format(tracking.averageDailyExpenseCents) + " / day")
                    Button(onClick = onOpenActivity) { Text("View Activity") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Plan Mode", style = MaterialTheme.typography.titleLarge)
                    if (plan == null) {
                        Text("Create a future plan, then compare real transactions against that fixed plan.")
                        Button(onClick = onOpenPlan) { Text("Create Plan") }
                    } else {
                        val planRange = DateRange(
                            startOfDay(plan.startDateMillis),
                            endOfDay(plan.endDateMillis)
                        )
                        val analysis = calculatePlanAnalysis(plan, expenses, incomes, planRange)

                        Text("Health: " + analysis.healthPercent + "%")
                        Text("Planned spending: ₹" + ExpenseAmount.format(plan.plannedExpenses.values.sum()))
                        Text("Actual spending: ₹" + ExpenseAmount.format(analysis.actualSpendingForPeriodCents))
                        Text(
                            if (analysis.spendingVarianceCents >= 0L)
                                "Above plan by ₹" + ExpenseAmount.format(analysis.spendingVarianceCents)
                            else
                                "Below plan by ₹" + ExpenseAmount.format(-analysis.spendingVarianceCents)
                        )
                        Button(onClick = onOpenPlan) { Text("Open Plan") }
                    }
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
