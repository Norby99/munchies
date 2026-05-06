package com.munchies.restaurant.domain.valueobject.menu

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay

/**
 * Represents the validity of a menu item, which can be defined by various criteria
 * such as date ranges, specific days of the week, or time ranges.
 * In the ranges the start date/time is inclusive and the end date/time is inclusive as well.
 */
sealed interface Validity {
  fun isValid(date: LocalDateTime): Boolean
  fun combine(other: Validity): Validity = And(this, other)

  companion object {
    fun period(start: LocalDate, end: LocalDate): Validity = Period(start, end)
    fun yearly(start: MonthDay, end: MonthDay): Validity = Yearly(start, end)
    fun weekly(vararg days: DayOfWeek): Validity = Weekly(days.toSet())
    fun from(start: LocalDate): Validity = Period(start, LocalDate.MAX)
    fun until(end: LocalDate): Validity = Period(LocalDate.MIN, end)
    val always: Validity = Always

    fun hours(start: LocalTime, end: LocalTime): Validity = Hours(start, end)
  }

  data class And(val first: Validity, val second: Validity) : Validity {
    override fun isValid(date: LocalDateTime): Boolean = first.isValid(date) && second.isValid(date)
  }

  data class Hours(val start: LocalTime, val end: LocalTime) : Validity {
    override fun isValid(date: LocalDateTime): Boolean {
      val time = date.toLocalTime()
      return if (!start.isAfter(end)) {
        !time.isBefore(start) && !time.isAfter(end)
      } else {
        !time.isBefore(start) || !time.isAfter(end)
      }
    }
  }

  data class Period(val start: LocalDate, val end: LocalDate) : Validity {
    init {
      require(start.isBefore(end)) { "Period start must be before end" }
    }
    override fun isValid(date: LocalDateTime): Boolean =
      !date.toLocalDate().isBefore(start) && !date.toLocalDate().isAfter(end)
  }

  data class Yearly(val start: MonthDay, val end: MonthDay) : Validity {
    override fun isValid(date: LocalDateTime): Boolean {
      val monthDay = MonthDay.from(date)
      return if (!start.isAfter(end)) {
        !monthDay.isBefore(start) && !monthDay.isAfter(end)
      } else {
        !monthDay.isBefore(start) || !monthDay.isAfter(end)
      }
    }
  }

  data class Weekly(val days: Set<DayOfWeek>) : Validity {
    override fun isValid(date: LocalDateTime): Boolean = days.contains(date.dayOfWeek)
  }

  data object Always : Validity {
    override fun isValid(date: LocalDateTime): Boolean = true
  }
}
