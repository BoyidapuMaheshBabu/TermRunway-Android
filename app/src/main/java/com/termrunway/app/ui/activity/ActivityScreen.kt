@file:OptIn(ExperimentalMaterial3Api::class)
package com.termrunway.app.ui.activity

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.termrunway.app.logic.trackingRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(TrackingPeriod.SEVEN_DAYS) }
    // Hardcoded custom range for now. In a real app we'd have a date picker.
    val customRange = remember {
        val today = java.util.Calendar.getInstance().timeInMillis
        DateRange(today - 14L * 24 * 60 * 60 * 1000, today)
    }

    val range = trackingRange(period, customRange = customRange)
    val transactions = buildList {
        expenses.filter { it.dateMillis in range.startMillis..range.endMillis }.forEach {
            add(ActivityItem(it.dateMillis, false, it.amountCents, it.category))
        }
        incomes.filter { it.dateMillis in range.startMillis..range.endMillis }.forEach {
            add(ActivityItem(it.dateMillis, true, it.amountCents, it.source))
        }
    }.sortedByDescending { it.dateMillis }

    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amountCents }
    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amountCents }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Activity") })
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Actual money activity", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Income ₹" + ExpenseAmount.format(totalIncome) +
                            "  •  Spending ₹" + ExpenseAmount.format(totalExpense),
                        style = MaterialTheme.typography.bodyMedium
                    )
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
            }

            if (transactions.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No transactions in this period.")
                            Text("Use + to record income or expense.")
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                if (item.isIncome) "Income" else "Expense",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(item.label, style = MaterialTheme.typography.titleMedium)
                            Text("₹" + ExpenseAmount.format(item.amountCents))
                            Text(
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(Date(item.dateMillis)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ActivityItem(
    val dateMillis: Long,
    val isIncome: Boolean,
    val amountCents: Long,
    val label: String,
    val id: String = label + dateMillis.toString() + amountCents.toString()
)
