package com.munchies.restaurant.domain.valueobject.menu

import java.time.LocalDate
import java.time.MonthDay

sealed interface Validity {
  fun isValid(date: LocalDate): Boolean

  data class Period(val start: LocalDate, val end: LocalDate) : Validity {
    init {
      require(start.isBefore(end)) { "Period start must be before end" }
    }
    override fun isValid(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
  }

  data class Yearly(val start: MonthDay, val end: MonthDay) : Validity {
    override fun isValid(date: LocalDate): Boolean {
      val monthDay = MonthDay.from(date)
      return if (!start.isAfter(end)) {
        !monthDay.isBefore(start) && !monthDay.isAfter(end)
      } else {
        !monthDay.isBefore(start) || !monthDay.isAfter(end)
      }
    }
  }

  data class From(val start: LocalDate) : Validity {
    override fun isValid(date: LocalDate): Boolean = !date.isBefore(start)
  }

  data class Until(val end: LocalDate) : Validity {
    override fun isValid(date: LocalDate): Boolean = !date.isAfter(end)
  }

  data object Always : Validity {
    override fun isValid(date: LocalDate): Boolean = true
  }
}
