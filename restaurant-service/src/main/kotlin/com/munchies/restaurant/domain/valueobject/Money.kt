package com.munchies.restaurant.domain.valueobject

import java.math.BigDecimal

/**
 * Value object representing a monetary amount.
 * Using this ensures type safety and centralizes rounding, comparison, and formatting rules.
 */
data class Money(val amount: BigDecimal) {
  init {
    require(amount >= BigDecimal.ZERO) { "Money amount cannot be negative" }
  }

  operator fun plus(other: Money): Money {
    return Money(this.amount.add(other.amount))
  }

  operator fun minus(other: Money): Money {
    return Money(this.amount.subtract(other.amount))
  }

  operator fun times(multiplier: Int): Money {
    return Money(this.amount.multiply(BigDecimal(multiplier)))
  }

  operator fun compareTo(other: Money): Int {
    return this.amount.compareTo(other.amount)
  }

  companion object {
    operator fun invoke(value: String): Money {
      return Money(BigDecimal(value))
    }

    operator fun invoke(value: Double): Money {
      return Money(BigDecimal(value))
    }

    operator fun invoke(value: Int): Money {
      return Money(BigDecimal(value))
    }
  }
}
