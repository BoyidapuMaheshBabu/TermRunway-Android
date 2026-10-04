package com.termrunway.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.termrunway.app.R
import com.termrunway.app.data.Category
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.mode.TrackingMode

@Composable
fun AppLogoMark(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = R.drawable.termrunway_logo),
        contentDescription = "TermRunway logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SectionTitle(title: String, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun MoneyText(
    amountPaise: Long,
    prefixPlus: Boolean = false,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val sign = if (amountPaise < 0) "-" else if (prefixPlus && amountPaise > 0) "+" else ""
    val absolute = kotlin.math.abs(amountPaise)
    val amount = String.format(java.util.Locale.getDefault(), "%,.2f", absolute / 100.0)
    Text("$sign₹$amount", modifier = modifier, color = color)
}

@Composable
fun AmountCard(
    label: String,
    amountPaise: Long,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
            Spacer(Modifier.height(5.dp))
            MoneyText(amountPaise, modifier = Modifier, color = color)
            if (supporting != null) {
                Spacer(Modifier.height(4.dp))
                Text(supporting, style = MaterialTheme.typography.labelSmall, color = RunwayMuted)
            }
        }
    }
}

@Composable
fun MetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    AssistChip(onClick = {}, label = { Text("$label  $value", maxLines = 1, overflow = TextOverflow.Ellipsis) }, modifier = modifier)
}

@Composable
fun ModeToggle(selectedMode: TrackingMode, onChange: (TrackingMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == TrackingMode.DAILY,
            onClick = { onChange(TrackingMode.DAILY) },
            label = { Text("Daily Tracking") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedMode == TrackingMode.PLAN,
            onClick = { onChange(TrackingMode.PLAN) },
            label = { Text("Plan Tracking") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickAddCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = RunwayMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TransactionIcon(category: String, income: Boolean) {
    val icon = iconForCategory(category)
    Box(
        modifier = Modifier.size(42.dp).clip(CircleShape)
            .background(if (income) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if (income) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun ProgressAmountBar(
    value: Long,
    maxValue: Long,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    if (label != null) Text(label, style = MaterialTheme.typography.labelMedium, color = RunwayMuted)
    LinearProgressIndicator(
        progress = { (value.toDouble() / maxValue.coerceAtLeast(1).toDouble()).toFloat().coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EmptyState(title: String, description: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(description, color = RunwayMuted)
        if (action != null && onAction != null) {
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    selected: String,
    onAddCategory: (() -> Unit)? = null,
    onSelected: (String) -> Unit
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category.name == selected,
                onClick = { onSelected(category.name) },
                label = { Text(category.name) },
                leadingIcon = { Icon(iconForCategory(category.name), null) }
            )
        }
        if (onAddCategory != null) {
            AssistChip(
                onClick = onAddCategory,
                label = { Text("+ Add category") },
                leadingIcon = { Icon(Icons.Outlined.Add, null) }
            )
        }
    }
}

fun iconForCategory(category: String): ImageVector = when (category.lowercase()) {
    "food" -> Icons.Outlined.Fastfood
    "transport" -> Icons.Outlined.DirectionsBus
    "education" -> Icons.Outlined.School
    "bills" -> Icons.Outlined.ReceiptLong
    "shopping" -> Icons.Outlined.ShoppingBag
    "entertainment" -> Icons.Outlined.MoreHoriz
    "personal" -> Icons.Outlined.Person
    "parents", "parents / allowance" -> Icons.Outlined.FamilyRestroom
    "scholarship" -> Icons.Outlined.Savings
    "part-time" -> Icons.Outlined.Work
    "freelance" -> Icons.Outlined.Laptop
    "wallet" -> Icons.Outlined.Wallet
    "other" -> Icons.Outlined.Category
    else -> Icons.Outlined.Paid
}

fun todayLabel(): String =
    java.text.SimpleDateFormat("EEEE, dd MMMM", java.util.Locale.getDefault()).format(java.util.Date())

fun dateLabel(ms: Long): String =
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(ms))

fun moneyString(paise: Long): String {
    val sign = if (paise < 0) "-" else ""
    return sign + "₹" + String.format(java.util.Locale.getDefault(), "%,.2f", kotlin.math.abs(paise) / 100.0)
}
