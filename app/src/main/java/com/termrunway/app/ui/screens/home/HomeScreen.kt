package com.termrunway.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Transaction
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.GuidanceCard
import com.termrunway.app.ui.components.ModeToggle
import com.termrunway.app.ui.components.MoneyText
import com.termrunway.app.ui.components.PlanEmptyCard
import com.termrunway.app.ui.components.PlanHeroCard
import com.termrunway.app.ui.components.QuickAddCard
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.ui.components.moneyString
import com.termrunway.app.ui.components.todayLabel
import com.termrunway.app.ui.mode.TrackingMode
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.util.endOfDay

@Composable
fun HomeScreen(
    state: AppUiState,
    trackingMode: TrackingMode,
    onSettings: () -> Unit,
    onTrackingMode: (TrackingMode) -> Unit,
    onAdd: () -> Unit,
    onTransaction: (Transaction) -> Unit,
    onPlanEdit: () -> Unit
) {
    val metrics = state.activePlan?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "TermRunway",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (trackingMode == TrackingMode.DAILY) "Daily money tracker" else "Plan tracking & runway",
                        color = RunwayMuted
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        // Greeting
        item {
            Column {
                Text(
                    "Hi, ${state.name} 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(todayLabel(), color = RunwayMuted)
            }
        }

        // Tracking Mode Toggle
        item { ModeToggle(selectedMode = trackingMode, onChange = onTrackingMode) }

        // Mode-Aware Presentation
        if (trackingMode == TrackingMode.DAILY) {
            item {
                MainBalanceCard(
                    balance = state.balancePaise,
                    income = state.totalIncomePaise,
                    expense = state.totalExpensePaise
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AmountCard("Spent today", state.todayExpensePaise, RunwayRed, Modifier.weight(1f))
                    AmountCard("Income today", state.todayIncomePaise, RunwayMint, Modifier.weight(1f))
                }
            }
            item {
                QuickAddCard(
                    "Record a transaction",
                    "Add income or an expense in a few taps.",
                    onAdd
                )
            }
            item { SectionTitle("Recent activity") }
            val recent = state.transactions.take(5)
            if (recent.isEmpty()) {
                item {
                    EmptyState(
                        "No transactions yet",
                        "Start by recording the first thing that happened to your money.",
                        "Add transaction",
                        onAdd
                    )
                }
            } else {
                items(recent, key = { it.id }) { tx ->
                    TransactionRow(tx, onClick = { onTransaction(tx) })
                }
            }
            item {
                if (state.activePlan == null) {
                    OutlinedCard(
                        onClick = onPlanEdit,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text("Want to plan a semester?", fontWeight = FontWeight.Bold)
                                Text("Create a Plan Tracking runway when you're ready.", color = RunwayMuted)
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                } else {
                    OutlinedCard(
                        onClick = { onTrackingMode(TrackingMode.PLAN) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text("Active plan: ${state.activePlan.name}", fontWeight = FontWeight.Bold)
                                Text(
                                    "${metrics?.daysRemaining ?: 0} days remaining · ${moneyString(metrics?.actualRemainingPaise ?: 0L)} runway",
                                    color = RunwayMuted
                                )
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        } else {
            if (state.activePlan == null) {
                item { PlanEmptyCard(onCreate = onPlanEdit) }
            } else {
                item {
                    PlanHeroCard(
                        plan = state.activePlan,
                        metrics = metrics,
                        onEdit = onPlanEdit
                    )
                }
                if (metrics != null) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AmountCard(
                                "Remaining runway",
                                metrics.actualRemainingPaise,
                                if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed,
                                Modifier.weight(1f)
                            )
                            AmountCard(
                                "Available per day",
                                metrics.availablePerDayPaise,
                                if (metrics.availablePerDayPaise >= 0) RunwayBlue else RunwayRed,
                                Modifier.weight(1f)
                            )
                        }
                    }
                    item { GuidanceCard(metrics) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AmountCard("Expected remaining", metrics.expectedRemainingPaise, modifier = Modifier.weight(1f))
                            AmountCard("Actual spent", metrics.actualExpensePaise, RunwayRed, Modifier.weight(1f))
                        }
                    }
                }
                item {
                    SectionTitle(
                        "Recent plan activity",
                        action = { TextButton(onClick = onAdd) { Text("Add") } }
                    )
                }
                val planTransactions = state.transactions.filter { tx ->
                    val p = state.activePlan
                    p != null && tx.dateMs in p.startMs..endOfDay(p.endMs)
                }.take(5)

                if (planTransactions.isEmpty()) {
                    item {
                        EmptyState(
                            "No plan activity yet",
                            "Transactions recorded during your plan period will automatically adjust your runway.",
                            "Add transaction",
                            onAdd
                        )
                    }
                } else {
                    items(planTransactions, key = { it.id }) { tx ->
                        TransactionRow(tx, onClick = { onTransaction(tx) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MainBalanceCard(balance: Long, income: Long, expense: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Recorded balance", color = RunwayMuted, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            MoneyText(balance, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                MiniAmount("Income", income, RunwayMint)
                MiniAmount("Expenses", expense, RunwayRed)
            }
        }
    }
}

@Composable
private fun MiniAmount(label: String, amount: Long, color: Color) {
    Column {
        Text(label, color = RunwayMuted, style = MaterialTheme.typography.labelSmall)
        MoneyText(amount, prefixPlus = label == "Income", color = color)
    }
}
