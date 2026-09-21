package com.termrunway.app.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.termrunway.app.logic.RunwayForecast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    plan: TermPlan?,
    forecast: RunwayForecast?,
    modifier: Modifier = Modifier
) {
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

    val categoryTotals = termExpenses
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amountCents } }
        .entries
        .sortedByDescending { it.value }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Charts") }) },
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Income vs Expenses", style = MaterialTheme.typography.titleLarge)
                    val maxAmount = maxOf(totalIncome, totalExpense, 1L)
                    ChartBar("Income", totalIncome, maxAmount)
                    ChartBar("Expenses", totalExpense, maxAmount)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Recorded values for the selected period.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Spending by Category", style = MaterialTheme.typography.titleLarge)

                    if (categoryTotals.isEmpty()) {
                        Text(
                            "No expenses recorded for the selected period yet.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val maxCategory = categoryTotals.maxOf { it.value }.coerceAtLeast(1L)
                        categoryTotals.forEach { (category, amount) ->
                            ChartBar(category, amount, maxCategory)
                        }
                    }
                }
            }

            forecast?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Runway Snapshot", style = MaterialTheme.typography.titleLarge)
                        Text("Days remaining: " + it.daysRemaining)
                        Text("Daily safe spending: ₹" + ExpenseAmount.format(it.dailySafeSpendingCents))
                        Text("Projected balance: ₹" + ExpenseAmount.format(it.projectedBalanceCents))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartBar(
    label: String,
    valueCents: Long,
    maxCents: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("₹" + ExpenseAmount.format(valueCents))
        }

        val fraction = (valueCents.toFloat() / maxCents.toFloat()).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(6.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}
