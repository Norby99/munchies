package com.munchies.order.domain.model

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String) {
  init { require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" } }
  operator fun plus(other: Money): Money {
    require(currency == other.currency) { "Currency mismatch" }
    return Money(amount + other.amount, currency)
  }
}
