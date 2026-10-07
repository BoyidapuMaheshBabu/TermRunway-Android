package com.termrunway.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed

@Composable
fun PlanHeroCard(
    plan: FinancialPlan,
    metrics: PlanMetrics?,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(
                        dateLabel(plan.startMs) + " → " + dateLabel(plan.endMs),
                        color = RunwayMuted
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit plan") }
            }
            if (metrics != null) {
                ProgressAmountBar(
                    value = metrics.actualExpensePaise,
                    maxValue = (metrics.actualExpensePaise + metrics.actualRemainingPaise.coerceAtLeast(0)).coerceAtLeast(1),
                    label = "Runway money used"
                )
            }
        }
    }
}

@Composable
fun PlanSummaryGrid(
    metrics: PlanMetrics,
    modifier: Modifier = Modifier,
    onIncomeClick: (() -> Unit)? = null,
    onExpenseClick: (() -> Unit)? = null,
    onSafeToSpendClick: (() -> Unit)? = null,
    onRemainingDaysClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlanGridCard(
                label = "Income",
                primaryResult = "${moneyString(metrics.actualIncomePaise)} / ${moneyString(metrics.totalExpectedIncomePaise)}",
                supportingText = "actual / expected",
                primaryColor = RunwayMint,
                onClick = onIncomeClick,
                modifier = Modifier.weight(1f)
            )
            PlanGridCard(
                label = "Expenses",
                primaryResult = "${moneyString(metrics.actualExpensePaise)} / ${moneyString(metrics.totalPlannedExpensePaise)}",
                supportingText = "actual / planned",
                primaryColor = RunwayRed,
                onClick = onExpenseClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlanGridCard(
                label = "Safe to Spend Today",
                primaryResult = moneyString(metrics.safeToSpendTodayPaise),
                supportingText = "current runway guidance",
                primaryColor = if (metrics.safeToSpendTodayPaise > 0) RunwayBlue else RunwayRed,
                onClick = onSafeToSpendClick,
                modifier = Modifier.weight(1f)
            )
            PlanGridCard(
                label = "Remaining Days",
                primaryResult = "${metrics.daysRemaining} days",
                supportingText = "in this plan",
                onClick = onRemainingDaysClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PlanGridCard(
    label: String,
    primaryResult: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = RunwayMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = primaryResult,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = RunwayMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GuidanceCard(metrics: PlanMetrics) {
    val color = when (metrics.status) {
        "Overdrawn", "Above plan" -> RunwayRed
        "Below plan" -> RunwayBlue
        else -> RunwayMint
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.AutoGraph, null, tint = color)
            Column(Modifier.padding(start = 12.dp)) {
                Text("Plan Status", style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
                Text(metrics.status, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(metrics.guidance, color = RunwayMuted, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun PlanEmptyCard(onCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Build your runway", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(
                "Set a period, money available now, expected income and planned expenses. TermRunway will turn that into practical daily guidance.",
                color = RunwayMuted
            )
            Button(onClick = onCreate) { Text("Create plan") }
        }
    }
}
