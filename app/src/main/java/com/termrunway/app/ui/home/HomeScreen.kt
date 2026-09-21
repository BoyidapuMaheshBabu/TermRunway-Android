package com.termrunway.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.Income
import com.termrunway.app.logic.RunwayForecast
import com.termrunway.app.logic.RunwayStatus
import com.termrunway.app.logic.calculateFinancialSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenses: List<Expense> = emptyList(),
    incomes: List<Income> = emptyList(),
    forecast: RunwayForecast? = null,
    onViewHistory: () -> Unit = {},
    onPlanTerm: () -> Unit = {},
    onViewAnalysis: () -> Unit = {},
    onDecisionCheck: () -> Unit = {},
    onViewGoal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summary = calculateFinancialSummary(incomes, expenses)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TermRunway") }
            )
        },
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
            if (forecast != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Term Runway", style = MaterialTheme.typography.titleLarge)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Available Now:")
                            Text("₹${ExpenseAmount.format(forecast.availableNowCents)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expected Future Income:")
                            Text("₹${ExpenseAmount.format(forecast.expectedFutureIncomeCents)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remaining Planned Expenses:")
                            Text("₹${ExpenseAmount.format(forecast.remainingPlannedExpensesCents)}")
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Projected End Balance:", style = MaterialTheme.typography.titleMedium)
                            val balanceColor = if (forecast.projectedBalanceCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            Text("₹${ExpenseAmount.format(forecast.projectedBalanceCents)}", style = MaterialTheme.typography.titleMedium, color = balanceColor)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Days Remaining:")
                            Text("${forecast.daysRemaining}")
                        }
                        if (forecast.daysRemaining > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Safe Spending:")
                                Text("₹${ExpenseAmount.format(forecast.dailySafeSpendingCents)} / day")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusText = when (forecast.status) {
                            RunwayStatus.BUFFER -> "Buffer: Your projected balance leaves room for the rest of the term."
                            RunwayStatus.TIGHT -> "Tight: Your planned expenses are close to the money expected to be available."
                            RunwayStatus.SHORTFALL -> "Shortfall: Your planned expenses are higher than the money expected to be available."
                        }
                        val statusColor = when (forecast.status) {
                            RunwayStatus.BUFFER -> MaterialTheme.colorScheme.primary
                            RunwayStatus.TIGHT -> MaterialTheme.colorScheme.tertiary
                            RunwayStatus.SHORTFALL -> MaterialTheme.colorScheme.error
                        }
                        Text(statusText, style = MaterialTheme.typography.bodyMedium, color = statusColor)
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Financial Summary", style = MaterialTheme.typography.titleMedium)
                        Text("Available Balance: ₹${ExpenseAmount.format(summary.availableBalanceCents)}", style = MaterialTheme.typography.bodyLarge)
                        Text("Total Income: ₹${ExpenseAmount.format(summary.totalIncomeCents)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Total Expenses: ₹${ExpenseAmount.format(summary.totalExpenseCents)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Set up Term Planning to see your runway forecast.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Action grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onViewHistory, modifier = Modifier.weight(1f)) { Text("History") }
                    OutlinedButton(onClick = onPlanTerm, modifier = Modifier.weight(1f)) { Text("Term Planning") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onViewAnalysis, modifier = Modifier.weight(1f)) { Text("Spending Analysis") }
                    OutlinedButton(onClick = onDecisionCheck, modifier = Modifier.weight(1f)) { Text("Purchase Check") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onViewGoal, modifier = Modifier.weight(1f)) { Text("Savings Goal") }
                }
            }
        }
    }
}
