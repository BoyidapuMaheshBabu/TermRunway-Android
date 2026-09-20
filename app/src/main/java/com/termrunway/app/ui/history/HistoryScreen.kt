package com.termrunway.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.ui.components.ExpenseListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    expenses: List<Expense>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedExpenses = expenses.sortedByDescending { it.dateMillis }
    val totalSpent = sortedExpenses.sumOf { it.amountCents }
    val expenseLabel = if (sortedExpenses.size == 1) "expense" else "expenses"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense History") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (sortedExpenses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No expenses yet")
                Text("Your saved expenses will appear here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Total Spent")
                            Text("₹" + ExpenseAmount.format(totalSpent))
                            Text(sortedExpenses.size.toString() + " " + expenseLabel)
                        }
                    }
                }

                items(
                    items = sortedExpenses,
                    key = { it.id }
                ) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}
