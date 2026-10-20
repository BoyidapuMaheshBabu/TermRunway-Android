package com.termrunway.app.ui.screens.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.FilterChipRow
import com.termrunway.app.ui.components.ScreenHeader
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.util.PeriodPreset
import com.termrunway.app.ui.util.pickDate
import com.termrunway.app.ui.util.resolvePeriodRange
import java.time.LocalDate

@Composable
fun ActivityScreen(
    state: AppUiState,
    onSettings: () -> Unit,
    onTransaction: (Transaction) -> Unit
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(0) } // 0: All, 1: Income, 2: Expense
    var selectedPreset by rememberSaveable { mutableStateOf(PeriodPreset.ALL_TIME) }
    var customStart by rememberSaveable { mutableStateOf(LocalDate.now().minusDays(30)) }
    var customEnd by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val periodChoices = remember(state.activePlan) {
        buildList {
            add(PeriodPreset.ALL_TIME)
            add(PeriodPreset.DAYS_7)
            add(PeriodPreset.DAYS_30)
            add(PeriodPreset.DAYS_90)
            if (state.activePlan != null) {
                add(PeriodPreset.ACTIVE_PLAN)
            }
            add(PeriodPreset.CUSTOM)
        }
    }

    val resolvedRange = remember(selectedPreset, state.activePlan, customStart, customEnd) {
        resolvePeriodRange(selectedPreset, state.activePlan, customStart, customEnd)
    }

    val filtered = remember(state.transactions, query, mode, resolvedRange) {
        state.transactions.filter { tx ->
            val modeMatch = when (mode) {
                1 -> tx.type == TransactionType.INCOME
                2 -> tx.type == TransactionType.EXPENSE
                else -> true
            }
            val queryMatch = query.isBlank() ||
                tx.category.contains(query, true) ||
                tx.description.contains(query, true)
            val rangeMatch = if (resolvedRange.start == null || resolvedRange.end == null) {
                true
            } else {
                !tx.date.isBefore(resolvedRange.start) && !tx.date.isAfter(resolvedRange.end)
            }
            modeMatch && queryMatch && rangeMatch
        }
    }

    val periodIncome = remember(filtered) { FinancialCalculator.totalByType(filtered, TransactionType.INCOME) }
    val periodExpense = remember(filtered) { FinancialCalculator.totalByType(filtered, TransactionType.EXPENSE) }
    val periodNet = periodIncome - periodExpense

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ScreenHeader(
                title = "Activity",
                subtitle = "See what actually happened",
                onSettings = onSettings
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search transactions") },
                singleLine = true
            )
        }
        item {
            FilterChipRow(
                values = listOf("All", "Income", "Expense"),
                selected = mode,
                onSelected = { mode = it }
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periodChoices.forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { selectedPreset = preset },
                        label = { Text(preset.label) }
                    )
                }
            }
        }
        if (selectedPreset == PeriodPreset.CUSTOM) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pickDate(context, customStart) { customStart = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text("From: " + dateLabel(customStart), maxLines = 1) }
                    OutlinedButton(
                        onClick = { pickDate(context, customEnd) { customEnd = it } },
                        modifier = Modifier.weight(1f)
                    ) { Text("To: " + dateLabel(customEnd), maxLines = 1) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AmountCard("Income", periodIncome, RunwayMint, Modifier.weight(1f))
                AmountCard("Spent", periodExpense, RunwayRed, Modifier.weight(1f))
                AmountCard("Net", periodNet, if (periodNet >= 0) RunwayMint else RunwayRed, Modifier.weight(1f))
            }
        }
        item { SectionTitle("Transactions (${filtered.size})") }
        if (filtered.isEmpty()) {
            item { EmptyState("No transactions in this period", "Try selecting a broader date range or a different filter.") }
        } else {
            items(filtered, key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { onTransaction(tx) })
            }
        }
    }
}
