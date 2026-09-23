package com.termrunway.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.Income
import com.termrunway.app.domain.parseMoneyToCents
import com.termrunway.app.util.dayOnlyWithTime
import com.termrunway.app.util.formatDay
import com.termrunway.app.util.mergeDateKeepingTime
import com.termrunway.app.util.startOfDay
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditor(
    request: EditorRequest,
    currentData: AppData,
    onDismiss: () -> Unit,
    onSave: (AppData) -> Unit,
    onDelete: (AppData) -> Unit
) {
    val existingExpense = request.expense
    val existingIncome = request.income
    val editing = existingExpense != null || existingIncome != null

    var isExpense by remember(request) {
        mutableStateOf(
            when {
                existingIncome != null -> false
                existingExpense != null -> true
                request.kind == EditorRequest.Kind.INCOME -> false
                else -> true
            }
        )
    }
    var amountText by remember(request) {
        mutableStateOf(
            when {
                existingExpense != null -> (existingExpense.amountCents / 100.0).toString()
                existingIncome != null -> (existingIncome.amountCents / 100.0).toString()
                else -> ""
            }
        )
    }
    var category by remember(request) {
        mutableStateOf(existingExpense?.category ?: ExpenseCategories.ALL.first())
    }
    var source by remember(request) {
        mutableStateOf(existingIncome?.source ?: "")
    }
    var note by remember(request) {
        mutableStateOf(existingExpense?.note ?: existingIncome?.note.orEmpty())
    }
    var selectedDay by remember(request) {
        mutableStateOf(
            startOfDay(
                existingExpense?.dateMillis
                    ?: existingIncome?.dateMillis
                    ?: request.defaultDateMillis
            )
        )
    }
    var categoryDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (editing) "Edit transaction" else "Add transaction",
                style = MaterialTheme.typography.titleLarge
            )

            if (!editing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = {
                            isExpense = true
                            errorMessage = null
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = {
                            isExpense = false
                            errorMessage = null
                        },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { character ->
                        character.isDigit() || character == '.'
                    }.take(14)
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            if (isExpense) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = {
                        TextButton(onClick = { categoryDialog = true }) {
                            Text("Change")
                        }
                    }
                )
            } else {
                OutlinedTextField(
                    value = source,
                    onValueChange = {
                        source = it.take(50)
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Income source") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = formatDay(selectedDay),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = ::chooseDate) {
                        Text("Change")
                    }
                }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(120) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                minLines = 2,
                maxLines = 4
            )

            errorMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val amount = parseMoneyToCents(amountText)
                    when {
                        amount == null -> {
                            errorMessage = "Enter a valid amount greater than ₹0."
                        }
                        isExpense && category.isBlank() -> {
                            errorMessage = "Choose a category."
                        }
                        !isExpense && source.trim().isBlank() -> {
                            errorMessage = "Enter an income source."
                        }
                        else -> {
                            val oldDate = existingExpense?.dateMillis ?: existingIncome?.dateMillis
                            val newDate = if (oldDate != null) {
                                if (startOfDay(oldDate) == selectedDay) {
                                    oldDate
                                } else {
                                    mergeDateKeepingTime(selectedDay, oldDate)
                                }
                            } else {
                                dayOnlyWithTime(
                                    selectedDay,
                                    preferCurrentTime =
                                        selectedDay == startOfDay(System.currentTimeMillis())
                                )
                            }

                            val updated = if (isExpense) {
                                val expense = Expense(
                                    id = existingExpense?.id ?: UUID.randomUUID().toString(),
                                    amountCents = amount,
                                    category = category.trim(),
                                    dateMillis = newDate,
                                    note = note.trim()
                                )
                                currentData.copy(
                                    expenses = currentData.expenses
                                        .filterNot { it.id == expense.id }
                                        .plus(expense)
                                        .sortedByDescending { it.dateMillis }
                                )
                            } else {
                                val income = Income(
                                    id = existingIncome?.id ?: UUID.randomUUID().toString(),
                                    amountCents = amount,
                                    source = source.trim(),
                                    dateMillis = newDate,
                                    note = note.trim()
                                )
                                currentData.copy(
                                    incomes = currentData.incomes
                                        .filterNot { it.id == income.id }
                                        .plus(income)
                                        .sortedByDescending { it.dateMillis }
                                )
                            }

                            onSave(updated)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editing) "Save changes" else "Save transaction")
            }

            if (editing) {
                TextButton(
                    onClick = { deleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete transaction", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (categoryDialog) {
        AlertDialog(
            onDismissRequest = { categoryDialog = false },
            title = { Text("Choose category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExpenseCategories.ALL.forEach { option ->
                        Surface(
                            onClick = {
                                category = option
                                categoryDialog = false
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (option == category) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Text(option, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete this transaction?") },
            text = { Text("This removes the record from your local history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDialog = false
                        val updated = if (existingExpense != null) {
                            currentData.copy(
                                expenses = currentData.expenses.filterNot {
                                    it.id == existingExpense.id
                                }
                            )
                        } else {
                            currentData.copy(
                                incomes = currentData.incomes.filterNot {
                                    it.id == existingIncome!!.id
                                }
                            )
                        }
                        onDelete(updated)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
