package com.termrunway.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed

@OptIn(ExperimentalMaterial3Api::class)
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
                Text("Actual remaining", color = RunwayMuted, style = MaterialTheme.typography.labelLarge)
                MoneyText(
                    metrics.actualRemainingPaise,
                    color = if (metrics.actualRemainingPaise >= 0) RunwayMint else RunwayRed
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(metrics.status) })
                    AssistChip(onClick = {}, label = { Text("${metrics.daysRemaining} days left") })
                }
                ProgressAmountBar(
                    value = metrics.actualExpensePaise,
                    maxValue = (metrics.actualExpensePaise + metrics.actualRemainingPaise.coerceAtLeast(0)).coerceAtLeast(1),
                    label = "Money used"
                )
            }
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
                Text(metrics.status, fontWeight = FontWeight.Bold)
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
