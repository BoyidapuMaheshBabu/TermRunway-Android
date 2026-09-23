package com.termrunway.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.domain.dailyTotals
import com.termrunway.app.ui.components.EditorRequest
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.MetricCard
import com.termrunway.app.ui.components.TransactionItem
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.util.addDays
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.formatRupees
import com.termrunway.app.util.formatSignedRupees
import com.termrunway.app.util.formatTime
import com.termrunway.app.util.isToday
import com.termrunway.app.util.startOfDay
import java.util.Calendar

@Composable
fun TrackScreen(
    data: AppData,
    contentPadding: PaddingValues,
    onSettings: () -> Unit,
    onAdd: (Long) -> Unit,
    onEdit: (EditorRequest) -> Unit
) {
    var selectedDay by rememberSaveable { mutableLongStateOf(startOfDay(System.currentTimeMillis())) }
    val today = startOfDay(System.currentTimeMillis())
    val totals = dailyTotals(data.expenses, data.incomes, selectedDay)
    val dayExpenses = data.expenses.filter { startOfDay(it.dateMillis) == selectedDay }.sortedByDescending { it.dateMillis }
    val dayIncome = data.incomes.filter { startOfDay(it.dateMillis) == selectedDay }.sortedByDescending { it.dateMillis }
    val context = LocalContext.current

    fun chooseDate() {
        val initial = Calendar.getInstance().apply { timeInMillis = selectedDay }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                selectedDay = startOfDay(chosen)
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        ).also { it.datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Track", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("What actually happened", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedDay = addDays(selectedDay, -1) }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Previous day")
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isToday(selectedDay)) "Today" else formatDay(selectedDay), fontWeight = FontWeight.SemiBold)
                    if (isToday(selectedDay)) {
                        Text(formatDay(selectedDay), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(
                    enabled = selectedDay.compareTo(today) < 0,
                    onClick = { selectedDay = addDays(selectedDay, 1) }
                ) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Next day")
                }
                TextButton(onClick = ::chooseDate) { Text("Calendar") }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Spent", formatRupees(totals.spentCents), Modifier.weight(1f))
                MetricCard("Income", formatRupees(totals.incomeCents), Modifier.weight(1f))
                MetricCard("Net", formatSignedRupees(totals.netCents), Modifier.weight(1f))
            }
        }

        if (data.dailyLimitCents > 0L) {
            item {
                val remaining = data.dailyLimitCents - totals.spentCents
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (remaining < 0L) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            if (remaining >= 0L) "${formatRupees(remaining)} left of your daily limit"
                            else "${formatRupees(-remaining)} above your daily limit",
                            fontWeight = FontWeight.SemiBold,
                            color = if (remaining < 0L) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text("The limit is informational only.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (dayIncome.isEmpty() && dayExpenses.isEmpty()) {
            item {
                EmptyState(
                    title = "No transactions for this day",
                    description = "Record the money that actually came in or went out.",
                    actionLabel = "Add transaction",
                    onAction = { onAdd(selectedDay) }
                )
            }
        } else {
            if (dayIncome.isNotEmpty()) {
                item { Text("Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(dayIncome, key = { "income-" + it.id }) { income ->
                    TransactionRow(
                        TransactionItem(
                            id = income.id,
                            title = income.source,
                            subtitle = formatTime(income.dateMillis) + if (income.note.isBlank()) "" else " · " + income.note,
                            amountCents = income.amountCents,
                            isIncome = true,
                            income = income
                        )
                    ) { onEdit(EditorRequest(income = income, defaultDateMillis = income.dateMillis)) }
                }
            }

            if (dayExpenses.isNotEmpty()) {
                item { Text("Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(dayExpenses, key = { "expense-" + it.id }) { expense ->
                    TransactionRow(
                        TransactionItem(
                            id = expense.id,
                            title = expense.category,
                            subtitle = formatTime(expense.dateMillis) + if (expense.note.isBlank()) "" else " · " + expense.note,
                            amountCents = expense.amountCents,
                            isIncome = false,
                            expense = expense
                        )
                    ) { onEdit(EditorRequest(expense = expense, defaultDateMillis = expense.dateMillis)) }
                }
            }
        }

        item {
            TextButton(onClick = { onAdd(selectedDay) }, modifier = Modifier.fillMaxWidth()) {
                Text("Add another transaction")
            }
        }
    }
}
