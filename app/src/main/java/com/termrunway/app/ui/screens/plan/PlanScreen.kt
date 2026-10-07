package com.termrunway.app.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.Transaction
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.GuidanceCard
import com.termrunway.app.ui.components.PlanEmptyCard
import com.termrunway.app.ui.components.PlanHeroCard
import com.termrunway.app.ui.components.PlanSummaryGrid
import com.termrunway.app.ui.components.ScreenHeader
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.components.moneyString
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.util.endOfDay

@Composable
fun PlanScreen(
    state: AppUiState,
    onSettings: () -> Unit,
    onCreateOrEdit: () -> Unit,
    onTransaction: (Transaction) -> Unit
) {
    val active = state.activePlan
    val metrics = active?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }
    val completed = state.plans.filter { active == null || it.id != active.id }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "Plan Tracking",
                subtitle = "Semester runway and daily spending guidance",
                onSettings = onSettings
            )
        }

        if (active == null) {
            item { PlanEmptyCard(onCreateOrEdit) }
        } else {
            item { PlanHeroCard(active, metrics, onCreateOrEdit) }

            if (metrics != null) {
                item { GuidanceCard(metrics) }

                item { SectionTitle("Financial summary") }
                item {
                    PlanSummaryGrid(
                        metrics = metrics
                    )
                }

                if (state.plannedIncomes.isNotEmpty()) {
                    item { SectionTitle("Expected income entries") }
                    items(state.plannedIncomes, key = { "pi-" + it.id + "-" + it.source }) { income ->
                        ItemizedPlanEntryRow(
                            title = income.source,
                            amount = income.amountPaise,
                            color = RunwayMint,
                            isIncome = true
                        )
                    }
                }

                if (state.plannedExpenses.isNotEmpty()) {
                    item { SectionTitle("Planned expense entries") }
                    items(state.plannedExpenses, key = { "pe-" + it.id + "-" + it.category }) { expense ->
                        ItemizedPlanEntryRow(
                            title = expense.category,
                            amount = expense.amountPaise,
                            color = RunwayRed,
                            isIncome = false
                        )
                    }
                }
            }

            item { SectionTitle("Recent plan activity") }
            val planTransactions = state.transactions.filter {
                it.dateMs in active.startMs..endOfDay(active.endMs)
            }.take(5)

            if (planTransactions.isEmpty()) {
                item {
                    EmptyState(
                        "No actual activity yet",
                        "Transactions recorded during your plan period will automatically adjust your runway."
                    )
                }
            } else {
                items(planTransactions, key = { it.id }) { tx ->
                    TransactionRow(tx, onClick = { onTransaction(tx) })
                }
            }

            if (completed.isNotEmpty()) {
                item { SectionTitle("Other plans") }
                items(completed, key = { it.id }) { plan ->
                    OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(plan.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    dateLabel(plan.startMs) + " → " + dateLabel(plan.endMs),
                                    color = RunwayMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemizedPlanEntryRow(
    title: String,
    amount: Long,
    color: Color,
    isIncome: Boolean
) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                    contentDescription = null,
                    tint = color
                )
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(if (isIncome) "Planned income" else "Planned expense", color = RunwayMuted, style = MaterialTheme.typography.labelSmall)
            }
            Text(moneyString(amount), color = color, fontWeight = FontWeight.Bold)
        }
    }
}
