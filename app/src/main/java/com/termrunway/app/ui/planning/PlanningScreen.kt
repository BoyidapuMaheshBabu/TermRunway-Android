package com.termrunway.app.ui.planning

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.ExpenseCategories
import com.termrunway.app.data.TermPlan
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    currentPlan: TermPlan?,
    onBack: () -> Unit,
    onSave: (TermPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDateMillis by remember { mutableStateOf(currentPlan?.startDateMillis ?: System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(currentPlan?.endDateMillis ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) }
    var startingFundsText by remember { mutableStateOf(currentPlan?.startingFundsCents?.let { ExpenseAmount.format(it) } ?: "") }
    var expectedIncomeText by remember { mutableStateOf(currentPlan?.expectedIncomeCents?.let { ExpenseAmount.format(it) } ?: "") }
    var planningMode by remember { mutableStateOf(currentPlan?.planningMode ?: "Semester") }
    
    val categoryAmounts = remember { mutableStateMapOf<String, String>() }
    
    LaunchedEffect(currentPlan) {
        if (currentPlan != null) {
            ExpenseCategories.ALL.forEach { category ->
                val amount = currentPlan.plannedExpenses[category]
                if (amount != null && amount > 0) {
                    categoryAmounts[category] = ExpenseAmount.format(amount)
                }
            }
        }
    }

    var generalError by remember { mutableStateOf<String?>(null) }
    
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val startDateText = remember(startDateMillis) { dateFormatter.format(Date(startDateMillis)) }
    val endDateText = remember(endDateMillis) { dateFormatter.format(Date(endDateMillis)) }
    
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Term Planning") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
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
            Text("Term Dates", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = planningMode == "Semester",
                    onClick = { planningMode = "Semester" }
                )
                Text("Semester")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = planningMode == "Monthly",
                    onClick = { planningMode = "Monthly" }
                )
                Text("Monthly")
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Start Date") },
                    trailingIcon = {
                        TextButton(onClick = {
                            calendar.timeInMillis = startDateMillis
                            DatePickerDialog(context, { _, year, month, day ->
                                calendar.set(year, month, day, 0, 0, 0)
                                startDateMillis = calendar.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) { Text("Change") }
                    }
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("End Date") },
                    trailingIcon = {
                        TextButton(onClick = {
                            calendar.timeInMillis = endDateMillis
                            DatePickerDialog(context, { _, year, month, day ->
                                calendar.set(year, month, day, 0, 0, 0)
                                endDateMillis = calendar.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) { Text("Change") }
                    }
                )
            }
            
            Text("Funds", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = startingFundsText,
                onValueChange = { startingFundsText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Available Money / Starting Funds") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            OutlinedTextField(
                value = expectedIncomeText,
                onValueChange = { expectedIncomeText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expected Future Income") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Planned Expenses", style = MaterialTheme.typography.titleMedium)
            
            ExpenseCategories.ALL.forEach { category ->
                OutlinedTextField(
                    value = categoryAmounts[category] ?: "",
                    onValueChange = { categoryAmounts[category] = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(category) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            
            Button(
                onClick = {
                    if (endDateMillis <= startDateMillis) {
                        generalError = "End date must be after start date."
                        return@Button
                    }
                    val startingFunds = ExpenseAmount.parseToCents(startingFundsText) ?: 0L
                    if (startingFunds < 0) {
                        generalError = "Starting funds cannot be negative."
                        return@Button
                    }
                    val expectedIncome = ExpenseAmount.parseToCents(expectedIncomeText) ?: 0L
                    if (expectedIncome < 0) {
                        generalError = "Expected income cannot be negative."
                        return@Button
                    }
                    
                    val planned = mutableMapOf<String, Long>()
                    ExpenseCategories.ALL.forEach { cat ->
                        val amt = ExpenseAmount.parseToCents(categoryAmounts[cat] ?: "")
                        if (amt != null && amt > 0) {
                            planned[cat] = amt
                        }
                    }
                    
                    generalError = null
                    onSave(TermPlan(
                        startDateMillis = startDateMillis,
                        endDateMillis = endDateMillis,
                        startingFundsCents = startingFunds,
                        expectedIncomeCents = expectedIncome,
                        planningMode = planningMode,
                        plannedExpenses = planned
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Plan")
            }
        }
    }
}
