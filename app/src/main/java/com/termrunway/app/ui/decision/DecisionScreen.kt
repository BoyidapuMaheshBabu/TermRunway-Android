package com.termrunway.app.ui.decision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.logic.RunwayForecast
import com.termrunway.app.logic.RunwayStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionScreen(
    forecast: RunwayForecast?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var purchaseName by remember { mutableStateOf("") }
    var purchaseAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Decision Check") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (forecast == null) {
                Text("Please set up a Term Plan to use this feature.", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Text("Check a potential purchase against your term runway.", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = purchaseName,
                onValueChange = { purchaseName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Purchase Name") },
                singleLine = true
            )

            OutlinedTextField(
                value = purchaseAmountText,
                onValueChange = { purchaseAmountText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Purchase Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            val purchaseAmountCents = ExpenseAmount.parseToCents(purchaseAmountText) ?: 0L
            val projectedAfter = forecast.projectedBalanceCents - purchaseAmountCents
            val dailySafeAfter = if (forecast.daysRemaining > 0) {
                (projectedAfter.coerceAtLeast(0L)) / forecast.daysRemaining
            } else {
                0L
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current projected balance:", style = MaterialTheme.typography.bodyMedium)
                    Text("₹${ExpenseAmount.format(forecast.projectedBalanceCents)}", style = MaterialTheme.typography.titleMedium)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Projected balance after purchase:", style = MaterialTheme.typography.bodyMedium)
                    val afterColor = if (projectedAfter < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    Text("₹${ExpenseAmount.format(projectedAfter)}", style = MaterialTheme.typography.titleMedium, color = afterColor)

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (forecast.daysRemaining > 0) {
                        Text("New daily safe spending:", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${ExpenseAmount.format(dailySafeAfter)} / day", style = MaterialTheme.typography.titleMedium)
                    }

                    if (projectedAfter < 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Warning: This purchase will result in a shortfall for the term.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
