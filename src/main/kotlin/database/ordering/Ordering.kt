package com.example.database.ordering

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

data class Ordering(
    val userId: Int,
    val status: Boolean,
    val formationDate: LocalDateTime,
    val weight: BigDecimal,
    val deliveryPrice: BigDecimal,
    val totalPrice: BigDecimal
)