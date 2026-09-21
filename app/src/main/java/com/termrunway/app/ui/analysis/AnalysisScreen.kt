package com.termrunway.app.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.TermPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    expenses: List<Expense>,
    plan: TermPlan?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val termExpenses = if (plan != null) {
        expenses.filter { it.dateMillis >= plan.startDateMillis && it.dateMillis <= plan.endDateMillis }
    } else {
        expenses
    }

    val totalSpending = termExpenses.sumOf { it.amountCents }
    val categoryTotals = termExpenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amountCents } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Analysis") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (termExpenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No expenses found for the current term.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Spending", style = MaterialTheme.typography.titleMedium)
                        Text("₹${ExpenseAmount.format(totalSpending)}", style = MaterialTheme.typography.headlineMedium)
                        Text("${termExpenses.size} expenses", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Text("Spending by Category", style = MaterialTheme.typography.titleLarge)

                categoryTotals.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                    val planned = plan?.plannedExpenses?.get(category) ?: 0L
                    CategoryBar(category, amount, planned)
                }
            }
        }
    }
}

@Composable
fun CategoryBar(category: String, amount: Long, planned: Long) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, style = MaterialTheme.typography.bodyLarge)
            Text("₹${ExpenseAmount.format(amount)}", style = MaterialTheme.typography.bodyLarge)
        }
        
        if (planned > 0) {
            Text("Planned: ₹${ExpenseAmount.format(planned)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(4.dp))
        
        val maxAmount = maxOf(amount, planned, 1L)
        val fillFraction = (amount.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
        
        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))) {
            Box(modifier = Modifier.fillMaxWidth(fillFraction).height(12.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)))
        }
    }
}
