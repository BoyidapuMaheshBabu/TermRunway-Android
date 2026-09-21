package com.termrunway.app.ui.reports

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.Income
import com.termrunway.app.data.SavingsGoal
import com.termrunway.app.data.TermPlan
import com.termrunway.app.logic.RunwayForecast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    plan: TermPlan?,
    forecast: RunwayForecast?,
    goal: SavingsGoal?,
    onOpenPlanning: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onOpenGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val termExpenses = if (plan != null) {
        expenses.filter { it.dateMillis >= plan.startDateMillis && it.dateMillis <= plan.endDateMillis }
    } else {
        expenses
    }

    val termIncomes = if (plan != null) {
        incomes.filter { it.dateMillis >= plan.startDateMillis && it.dateMillis <= plan.endDateMillis }
    } else {
        incomes
    }

    val totalExpense = termExpenses.sumOf { it.amountCents }
    val totalIncome = termIncomes.sumOf { it.amountCents }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reports") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (plan == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No term plan yet", style = MaterialTheme.typography.titleLarge)
                        Text("Set up a term to make reports use a defined period and planned spending.")
                        Button(onClick = onOpenPlanning) {
                            Text("Set Up Term")
                        }
                    }
                }
            }

            plan?.let { currentPlan ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Term Report", style = MaterialTheme.typography.titleLarge)
                        Text(
                            dateFormatter.format(Date(currentPlan.startDateMillis)) +
                                " – " +
                                dateFormatter.format(Date(currentPlan.endDateMillis))
                        )
                        SummaryRow("Starting funds", currentPlan.startingFundsCents)
                        SummaryRow("Expected income", currentPlan.expectedIncomeCents)
                        SummaryRow("Recorded income", totalIncome)
                        SummaryRow("Recorded expenses", totalExpense)
                        SummaryRow("Expense count", termExpenses.size.toLong(), isCurrency = false)

                        forecast?.let { currentForecast ->
                            SummaryRow("Projected end balance", currentForecast.projectedBalanceCents)
                            SummaryRow("Daily safe spending", currentForecast.dailySafeSpendingCents)
                            Text(
                                "Status: " + currentForecast.status.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Planned vs Actual", style = MaterialTheme.typography.titleLarge)

                        ExpenseCategories.ALL.forEach { category ->
                            val planned = currentPlan.plannedExpenses[category] ?: 0L
                            val actual = termExpenses
                                .filter { expense -> expense.category == category }
                                .sumOf { expense -> expense.amountCents }

                            if (planned > 0L || actual > 0L) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(category, modifier = Modifier.weight(1f))
                                    Text(
                                        "₹" + ExpenseAmount.format(actual) +
                                            " / ₹" + ExpenseAmount.format(planned)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            goal?.let { currentGoal ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Savings Goal", style = MaterialTheme.typography.titleLarge)
                        Text(currentGoal.name)
                        Text(
                            "₹" + ExpenseAmount.format(currentGoal.savedAmountCents) +
                                " / ₹" + ExpenseAmount.format(currentGoal.targetAmountCents)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                    Text("Open History")
                }
                OutlinedButton(onClick = onOpenAnalysis, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Spending Analysis")
                }
                OutlinedButton(onClick = onOpenGoal, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Savings Goal")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Long,
    isCurrency: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(if (isCurrency) "₹" + ExpenseAmount.format(value) else value.toString())
    }
}
