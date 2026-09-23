package com.termrunway.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.Income
import com.termrunway.app.ui.daily.formatLongDate
import com.termrunway.app.ui.daily.parseAmountCents
import com.termrunway.app.ui.daily.startOfDay
import java.util.Calendar

enum class TransactionType { EXPENSE, INCOME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditor(
    initialType: TransactionType,
    expense: Expense? = null,
    income: Income? = null,
    onDismiss: () -> Unit,
    onSaveExpense: (Expense) -> Unit,
    onSaveIncome: (Income) -> Unit,
    onDeleteExpense: (() -> Unit)? = null,
    onDeleteIncome: (() -> Unit)? = null
) {
    val editing = expense != null || income != null
    var type by rememberSaveable(expense?.id, income?.id) { mutableStateOf(initialType) }
    var amount by rememberSaveable(expense?.id, income?.id) {
        mutableStateOf(expense?.amountCents?.let { com.termrunway.app.ui.daily.formatWholeRupees(it) } ?: income?.amountCents?.let { com.termrunway.app.ui.daily.formatWholeRupees(it) } ?: "")
    }
    var category by rememberSaveable(expense?.id) { mutableStateOf(expense?.category ?: ExpenseCategories.ALL.first()) }
    var source by rememberSaveable(income?.id) { mutableStateOf(income?.source ?: "") }
    var note by rememberSaveable(expense?.id, income?.id) { mutableStateOf(expense?.note ?: income?.note.orEmpty()) }
    var dateMillis by rememberSaveable(expense?.id, income?.id) { mutableStateOf(expense?.dateMillis ?: income?.dateMillis ?: System.currentTimeMillis()) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var sourceError by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.imePadding()) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (type == TransactionType.INCOME) Icons.Outlined.Paid else Icons.Outlined.Wallet, null)
                Spacer(Modifier.width(8.dp))
                Text(if (editing) "Edit transaction" else "Add transaction", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            }

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEachIndexed { index, choice ->
                    SegmentedButton(
                        selected = type == choice,
                        onClick = { if (!editing) type = choice },
                        enabled = !editing,
                        shape = SegmentedButtonDefaults.itemShape(index, 2)
                    ) { Text(if (choice == TransactionType.EXPENSE) "Expense" else "Income") }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.length <= 16) { amount = it; amountError = null } },
                label = { Text("Amount") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (type == TransactionType.EXPENSE) {
                Text("Category", fontWeight = FontWeight.SemiBold)
                ExpenseCategories.ALL.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { item ->
                            AssistChip(
                                onClick = { category = item },
                                label = { Text(item, maxLines = 1) },
                                leadingIcon = { androidx.compose.material3.Icon(com.termrunway.app.ui.daily.categoryIcon(item), null, Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (category == item) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it; sourceError = null },
                    label = { Text("Income source") },
                    placeholder = { Text("Allowance, salary, scholarship...") },
                    isError = sourceError != null,
                    supportingText = { sourceError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(Icons.Outlined.CalendarMonth, null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Date", fontWeight = FontWeight.SemiBold)
                    Text(formatLongDate(dateMillis), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = {
                    val c = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val chosen = Calendar.getInstance().apply {
                                set(year, month, day, 12, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            dateMillis = startOfDay(chosen.timeInMillis)
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                    ).also { it.datePicker.maxDate = System.currentTimeMillis() }.show()
                }) { Text("Change") }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 120) note = it },
                label = { Text("Note (optional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (editing) {
                    OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.weight(1f)) {
                        androidx.compose.material3.Icon(Icons.Outlined.Delete, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Delete")
                    }
                }
                Button(
                    onClick = {
                        val cents = parseAmountCents(amount)
                        amountError = if (cents == null) "Enter an amount greater than ₹0." else null
                        sourceError = if (type == TransactionType.INCOME && source.trim().isBlank()) "Enter an income source." else null
                        if (cents != null && sourceError == null) {
                            if (type == TransactionType.EXPENSE) {
                                onSaveExpense(Expense(expense?.id ?: java.util.UUID.randomUUID().toString(), cents, category, dateMillis, note.trim()))
                            } else {
                                onSaveIncome(Income(income?.id ?: java.util.UUID.randomUUID().toString(), cents, source.trim(), dateMillis, note.trim()))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text(if (editing) "Save changes" else "Save") }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete transaction?") },
            text = { Text("This transaction will be removed from your tracking and insights.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    if (expense != null) onDeleteExpense?.invoke() else onDeleteIncome?.invoke()
                }) { Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
