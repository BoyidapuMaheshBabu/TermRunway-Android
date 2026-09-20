package com.termrunway.app.data

import java.util.UUID

data class Income(
    val id: String = UUID.randomUUID().toString(),
    val amountCents: Long,
    val source: String,
    val dateMillis: Long,
    val note: String = ""
)
