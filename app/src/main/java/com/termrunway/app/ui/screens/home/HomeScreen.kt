package com.termrunway.app.ui.screens.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoGraph
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
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.data.Transaction
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.GuidanceCard
import com.termrunway.app.ui.components.MoneyText
import com.termrunway.app.ui.components.QuickAddCard
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionRow
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.components.todayLabel
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import java.util.Calendar

data class GreetingText(
    val headline: String,
    val subtext: String
)

fun getGreeting(name: String, hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): GreetingText {
    val cleanName = name.trim()
    val namePart = if (cleanName.isNotEmpty()) ", $cleanName" else ""
    return when (hourOfDay) {
        in 5..11 -> GreetingText(
            headline = "Good morning$namePart! ☀️",
            subtext = "Let’s see how your money is doing today."
        )
        in 12..16 -> GreetingText(
            headline = "Good afternoon$namePart! 👋",
            subtext = "Here’s your money snapshot for today."
        )
        in 17..20 -> GreetingText(
            headline = "Good evening$namePart! 🌆",
            subtext = "Take a quick look at your progress today."
        )
        else -> GreetingText(
            headline = "Good night$namePart! 🌙",
            subtext = "Here’s where you stand before the day ends."
        )
    }
}

@Composable
fun HomeScreen(
    state: AppUiState,
    onSettings: () -> Unit,
    onAdd: () -> Unit,
    onTransaction: (Transaction) -> Unit,
    onPlanEdit: () -> Unit,
    onNavigateToActivity: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {}
) {
    val activePlan = state.activePlan
    val metrics = activePlan?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }
    val greeting = getGreeting(state.name)

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
                        "Money & Runway Dashboard",
                        color = RunwayMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            }
        }

        // Greeting Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    greeting.headline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    greeting.subtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    todayLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = RunwayMuted
                )
            }
        }

        // Daily Tracking Dashboard Section
        item { SectionTitle("Daily tracking") }

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

        // Plan Dashboard Section
        item {
            SectionTitle(
                "Plan overview",
                action = if (activePlan != null) {
                    {
                        TextButton(onClick = onNavigateToPlan) {
                            Text("Full plan →")
                        }
                    }
                } else null
            )
        }

        if (activePlan != null && metrics != null) {
            item {
                HomePlanHeroCard(
                    plan = activePlan,
                    metrics = metrics,
                    onViewPlan = onNavigateToPlan
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AmountCard(
                        "Remaining runway",
                        metrics.actualRemainingPaise,
                        if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed,
                        Modifier.weight(1f),
                        supporting = "${metrics.daysRemaining} days remaining"
                    )
                    AmountCard(
                        "Available per day",
                        metrics.availablePerDayPaise,
                        if (metrics.availablePerDayPaise >= 0) RunwayBlue else RunwayRed,
                        Modifier.weight(1f),
                        supporting = "Target daily budget"
                    )
                }
            }
            item { GuidanceCard(metrics) }
        } else {
            item {
                PlanInvitationCard(onCreatePlan = onPlanEdit)
            }
        }

        // Recent Transactions Section
        item {
            SectionTitle(
                "Recent activity",
                action = {
                    TextButton(onClick = onNavigateToActivity) {
                        Text("View more →")
                    }
                }
            )
        }

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
                MiniAmount("Total Income", income, RunwayMint)
                MiniAmount("Total Expenses", expense, RunwayRed)
            }
        }
    }
}

@Composable
private fun MiniAmount(label: String, amount: Long, color: Color) {
    Column {
        Text(label, color = RunwayMuted, style = MaterialTheme.typography.labelSmall)
        MoneyText(amount, prefixPlus = label.contains("Income"), color = color)
    }
}

@Composable
private fun HomePlanHeroCard(
    plan: FinancialPlan,
    metrics: PlanMetrics,
    onViewPlan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewPlan),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${dateLabel(plan.startMs)} → ${dateLabel(plan.endMs)} · ${metrics.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RunwayMuted
                    )
                }
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "View Plan details",
                    tint = RunwayMuted
                )
            }
        }
    }
}

@Composable
private fun PlanInvitationCard(onCreatePlan: () -> Unit) {
    OutlinedCard(
        onClick = onCreatePlan,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AutoGraph,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Want to plan ahead?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Create a plan to track semester runway, daily budgets, and expected expenses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RunwayMuted
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = RunwayMuted
            )
        }
    }
}
