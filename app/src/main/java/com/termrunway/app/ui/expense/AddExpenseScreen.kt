package com.termrunway.app.ui.expense

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.ExpenseCategories
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onSave: (Expense) -> Boolean,
    saveError: String? = null,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    val dateText = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    amountError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError != null,
                supportingText = {
                    amountError?.let { Text(it) }
                }
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Category") },
                    placeholder = { Text("Select a category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = categoryExpanded
                        )
                    },
                    isError = categoryError != null,
                    supportingText = {
                        categoryError?.let { Text(it) }
                    }
                )

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ExpenseCategories.ALL.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryError = null
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date") },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            calendar.timeInMillis = selectedDateMillis
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day, 12, 0, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    selectedDateMillis = calendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Text("Change")
                    }
                }
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = {
                    noteText = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                minLines = 3
            )

            saveError?.let {
                Text(it)
            }

            Spacer(modifier = Modifier)

            Button(
                onClick = {
                    val amountCents = ExpenseAmount.parseToCents(amountText)
                    amountError = when {
                        amountCents == null -> "Enter a positive amount with up to 2 decimal places."
                        else -> null
                    }

                    categoryError = if (selectedCategory == null) {
                        "Select a category."
                    } else {
                        null
                    }

                    if (amountCents != null && selectedCategory != null) {
                        onSave(
                            Expense(
                                amountCents = amountCents,
                                category = selectedCategory!!,
                                dateMillis = selectedDateMillis,
                                note = noteText.trim()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }
        }
    }
}
