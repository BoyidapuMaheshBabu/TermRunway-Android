package com.termrunway.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.ui.components.ExpenseListItem
import com.termrunway.app.ui.theme.TermRunwayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenses: List<Expense> = emptyList(),
    onAddExpense: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recentExpenses = expenses
        .sortedByDescending { it.dateMillis }
        .take(10)

    val totalSpent = expenses.sumOf { it.amountCents }
    val expenseLabel = if (expenses.size == 1) "expense" else "expenses"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TermRunway") }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Total Spent")
                        Text("₹" + ExpenseAmount.format(totalSpent))
                        Text(
                            expenses.size.toString() +
                                " " + expenseLabel +
                                " recorded"
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onAddExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Add Expense")
                }
            }

            item {
                Text(
                    text = "Recent Expenses",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 24.dp,
                        bottom = 8.dp
                    )
                )
            }

            if (recentExpenses.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded yet.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(
                    items = recentExpenses,
                    key = { it.id }
                ) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    TextButton(
                        onClick = onViewHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("View All Expenses")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TermRunwayTheme {
        HomeScreen()
    }
}
