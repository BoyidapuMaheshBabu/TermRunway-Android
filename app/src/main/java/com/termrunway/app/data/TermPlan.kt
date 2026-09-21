package com.termrunway.app.data

data class TermPlan(
    val startDateMillis: Long,
    val endDateMillis: Long,
    val startingFundsCents: Long,
    val expectedIncomeCents: Long,
    val planningMode: String,
    val plannedExpenses: Map<String, Long> // Category to amounts in cents
)
