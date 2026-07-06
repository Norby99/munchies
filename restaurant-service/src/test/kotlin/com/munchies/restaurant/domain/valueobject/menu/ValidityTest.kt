package com.munchies.restaurant.domain.valueobject.menu

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidityTest {
  @Test
  fun `period validity rejects when start is not strictly before end`() {
    val date = LocalDate.now()
    assertThrows<IllegalArgumentException> { Validity.period(date, date) }
    assertThrows<IllegalArgumentException> { Validity.period(date.plusDays(1), date) }
    assertThrows<IllegalArgumentException> {
      Validity.period(start = LocalDate.of(2024, 12, 31), end = LocalDate.of(2024, 1, 1))
    }
  }

  @Test
  fun `period validity respects start and end boundaries`() {
    val start = LocalDate.of(2024, 10, 1)
    val end = LocalDate.of(2024, 10, 31)
    val validity = Validity.period(start, end)

    assertFalse(validity.isValid(start.minusDays(1).atStartOfDay()))
    assertTrue(validity.isValid(start.atStartOfDay()))
    assertTrue(validity.isValid(start.plusDays(15).atStartOfDay()))
    assertTrue(validity.isValid(end.atStartOfDay()))
    assertFalse(validity.isValid(end.plusDays(1).atStartOfDay()))
  }

  @Test
  fun `yearly validity respects start and end boundaries within the same year`() {
    val start = MonthDay.of(6, 1)
    val end = MonthDay.of(8, 31)
    val yearly = Validity.yearly(start, end)

    assertFalse(yearly.isValid(LocalDate.of(2024, 5, 31).atStartOfDay()))
    assertTrue(yearly.isValid(LocalDate.of(2024, 6, 1).atStartOfDay()))
    assertTrue(yearly.isValid(LocalDate.of(2024, 8, 31).atStartOfDay()))
    assertFalse(yearly.isValid(LocalDate.of(2024, 9, 1).atStartOfDay()))
  }

  @Test
  fun `yearly validity supports wrap around across years`() {
    val validity = Validity.yearly(
      start = MonthDay.of(Month.SEPTEMBER, 1),
      end = MonthDay.of(Month.MAY, 31),
    )

    assertFalse(validity.isValid(LocalDate.of(2024, 8, 31).atStartOfDay()))
    assertTrue(validity.isValid(LocalDate.of(2024, 9, 1).atStartOfDay()))
    assertTrue(validity.isValid(LocalDate.of(2025, 5, 31).atStartOfDay()))
    assertFalse(validity.isValid(LocalDate.of(2025, 6, 1).atStartOfDay()))
  }

  @Test
  fun `from validity evaluates correctly at start boundary`() {
    val start = LocalDate.now()
    val from = Validity.from(start)

    assertFalse(from.isValid(start.minusDays(1).atStartOfDay()))
    assertTrue(from.isValid(start.atStartOfDay()))
    assertTrue(from.isValid(start.plusDays(1).atStartOfDay()))
  }

  @Test
  fun `until validity evaluates correctly at end boundary`() {
    val end = LocalDate.now()
    val until = Validity.until(end)

    assertTrue(until.isValid(end.minusDays(1).atStartOfDay()))
    assertTrue(until.isValid(end.atStartOfDay()))
    assertFalse(until.isValid(end.plusDays(1).atStartOfDay()))
  }

  @Test
  fun `always validity evaluates to true for any date`() {
    val always = Validity.always
    assertTrue(always.isValid(LocalDateTime.now()))
    assertTrue(always.isValid(LocalDateTime.MIN))
    assertTrue(always.isValid(LocalDateTime.MAX))
  }

  @Test
  fun `time range validity respects start and end time boundaries`() {
    val timeRange = Validity.hours(LocalTime.of(9, 0), LocalTime.of(17, 0))

    assertFalse(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 8, 59)))
    assertTrue(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 9, 0)))
    assertTrue(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 17, 0)))
    assertFalse(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 17, 1)))
  }

  @Test
  fun `time range validity supports wrap around across midnight`() {
    val timeRange = Validity.hours(LocalTime.of(22, 0), LocalTime.of(6, 0))

    assertFalse(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 21, 59)))
    assertTrue(timeRange.isValid(LocalDateTime.of(2024, 1, 1, 22, 0)))
    assertTrue(timeRange.isValid(LocalDateTime.of(2024, 1, 2, 6, 0)))
    assertFalse(timeRange.isValid(LocalDateTime.of(2024, 1, 2, 6, 1)))
  }

  @Test
  fun `composed validities with combine require all conditions to be met`() {
    val weekday = Validity.weekly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    val time = Validity.hours(LocalTime.of(18, 0), LocalTime.of(22, 0))
    val combined = weekday.combine(time)

    assertTrue(combined.isValid(LocalDateTime.of(2024, 5, 24, 19, 0)))
    assertFalse(combined.isValid(LocalDateTime.of(2024, 5, 24, 13, 0)))
    assertFalse(combined.isValid(LocalDateTime.of(2024, 5, 23, 19, 0)))
  }

  @Test
  fun `same periods should equal`() {
    val period1 = Validity.period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
    val period2 = Validity.period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))

    assertEquals(period1, period2)
  }
}
