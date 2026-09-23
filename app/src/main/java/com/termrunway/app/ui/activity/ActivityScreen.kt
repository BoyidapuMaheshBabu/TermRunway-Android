package com.termrunway.app.ui.activity

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.ui.daily.addDays
import com.termrunway.app.ui.daily.formatLongDate
import com.termrunway.app.ui.daily.formatRupees
import com.termrunway.app.ui.daily.formatSignedRupees
import com.termrunway.app.ui.daily.formatTime
import com.termrunway.app.ui.daily.isToday
import com.termrunway.app.ui.daily.startOfDay
import java.util.Calendar
import java.util.Locale

data class TransactionEditRequest(
    val expense: Expense? = null,
    val income: Income? = null
)

@Composable
fun ActivityScreen(
    expenses: List<Expense>,
    incomes: List<Income>,
    onSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onEdit: (TransactionEditRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(startOfDay(System.currentTimeMillis())) }
    var showDatePicker by remember { mutableStateOf(false) }
    val today = startOfDay(System.currentTimeMillis())

    val dayExpenses = expenses.filter { startOfDay(it.dateMillis) == selectedDate }.sortedByDescending { it.dateMillis }
    val dayIncome = incomes.filter { startOfDay(it.dateMillis) == selectedDate }.sortedByDescending { it.dateMillis }
    val spent = dayExpenses.sumOf { it.amountCents }
    val income = dayIncome.sumOf { it.amountCents }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Track", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Actual money activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Wallet, contentDescription = "Settings")
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { selectedDate = addDays(selectedDate, -1) }) {
                Icon(Icons.Outlined.ArrowBack, "Previous day")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isToday(selectedDate)) "Today" else formatLongDate(selectedDate), fontWeight = FontWeight.SemiBold)
                if (!isToday(selectedDate)) Text(formatLongDate(selectedDate), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { if (selectedDate < today) selectedDate = addDays(selectedDate, 1) }, enabled = selectedDate < today) {
                Icon(Icons.Outlined.ArrowForward, "Next day")
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Outlined.CalendarMonth, "Choose date")
            }
        }

        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Spent", formatRupees(spent), Modifier.weight(1f))
            SummaryCard("Income", formatRupees(income), Modifier.weight(1f))
            SummaryCard("Net", formatSignedRupees(income - spent), Modifier.weight(1f))
        }

        if (dayExpenses.isEmpty() && dayIncome.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Wallet, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(10.dp))
                Text("Nothing recorded", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("There are no transactions for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onAddExpense) { Text("Add expense") }
                    TextButton(onClick = onAddIncome) { Text("Add income") }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dayIncome.isNotEmpty()) {
                    item { Text("Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(dayIncome, key = { "income-" + it.id }) { item ->
                        TransactionCard(
                            title = item.source,
                            amount = item.amountCents,
                            income = true,
                            time = item.dateMillis,
                            note = item.note,
                            onClick = { onEdit(TransactionEditRequest(income = item)) }
                        )
                    }
                }
                if (dayExpenses.isNotEmpty()) {
                    item { Text("Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(dayExpenses, key = { "expense-" + it.id }) { item ->
                        TransactionCard(
                            title = item.category,
                            amount = item.amountCents,
                            income = false,
                            time = item.dateMillis,
                            note = item.note,
                            onClick = { onEdit(TransactionEditRequest(expense = item)) }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val initial = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(
            androidx.compose.ui.platform.LocalContext.current,
            { _, year, month, day ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis.let(::startOfDay)
                showDatePicker = false
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        ).also { it.datePicker.maxDate = System.currentTimeMillis() }.show()
        showDatePicker = false
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionCard(
    title: String,
    amount: Long,
    income: Boolean,
    time: Long,
    note: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (income) Icons.Outlined.Paid else Icons.Outlined.Wallet, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    formatTime(time) + if (note.isBlank()) "" else " · " + note,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text((if (income) "+" else "-") + formatRupees(amount), fontWeight = FontWeight.SemiBold)
        }
    }
}
