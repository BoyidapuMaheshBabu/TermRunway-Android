package com.termrunway.app.ui.daily

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.termrunway.app.data.Expense
import com.termrunway.app.data.ExpenseAmount
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

fun startOfDay(millis: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun addDays(millis: Long, amount: Int): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    calendar.add(Calendar.DAY_OF_YEAR, amount)
    return startOfDay(calendar.timeInMillis)
}

fun isToday(millis: Long): Boolean = startOfDay(millis) == startOfDay(System.currentTimeMillis())

fun parseAmountCents(input: String): Long? = runCatching {
    val clean = input.trim().replace(",", "")
    if (clean.isBlank()) return null
    BigDecimal(clean)
        .setScale(2, java.math.RoundingMode.UNNECESSARY)
        .movePointRight(2)
        .longValueExact()
        .takeIf { it > 0L }
}.getOrNull()

fun formatRupees(cents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return formatter.format(BigDecimal.valueOf(cents, 2))
}

fun formatWholeRupees(cents: Long): String = formatRupees(cents).removeSuffix(".00")

fun formatSignedRupees(cents: Long): String =
    if (cents < 0L) "-" + formatRupees(-cents) else formatRupees(cents)

fun formatLongDate(millis: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM · h:mm a", Locale.getDefault()).format(Date(millis))

fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

fun categoryIcon(label: String): ImageVector {
    val value = label.lowercase(Locale.getDefault())
    return when {
        "food" in value || "grocer" in value -> Icons.Outlined.Fastfood
        "book" in value || "tuition" in value || "academic" in value -> Icons.Outlined.MenuBook
        "transport" in value || "travel" in value -> Icons.Outlined.ArrowForward
        "personal" in value || "lifestyle" in value -> Icons.Outlined.Person
        "utility" in value || "internet" in value -> Icons.Outlined.AccountBalanceWallet
        else -> Icons.Outlined.Category
    }
}

fun recentAverage(expenses: List<Expense>, days: Int = 7): Long {
    val today = startOfDay(System.currentTimeMillis())
    val first = addDays(today, -(days - 1))
    val totalsByDay = expenses
        .filter { startOfDay(it.dateMillis) in first..today }
        .groupBy { startOfDay(it.dateMillis) }
        .values
        .map { day -> day.sumOf { it.amountCents } }
    return if (totalsByDay.isEmpty()) 0L else totalsByDay.sum() / totalsByDay.size
}

fun dailySpendingMessage(todaySpent: Long, average: Long): String? {
    if (todaySpent <= 0L || average <= 0L) return null
    val lower = average / 2L
    val higher = average * 18L / 10L
    return when {
        todaySpent >= higher -> "Today is higher than your recent spending average."
        todaySpent <= lower -> "Today is lower than your recent spending average."
        else -> null
    }
}

@Composable
fun SectionTitle(text: String) {
    androidx.compose.material3.Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary
    )
}
