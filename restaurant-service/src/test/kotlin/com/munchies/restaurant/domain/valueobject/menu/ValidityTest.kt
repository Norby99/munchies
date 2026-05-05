package com.munchies.restaurant.domain.valueobject.menu

import com.munchies.restaurant.domain.valueobject.menu.Validity.Period
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidityTest {
  @Test
  fun `should create period successfully`() {
    val start = LocalDate.now()
    val end = start.plusDays(1)
    val period = Period(start, end)

    assertEquals(start, period.start)
    assertEquals(end, period.end)
  }

  @Test
  fun `should throw when period start is not before end`() {
    val date = LocalDate.now()
    assertThrows<IllegalArgumentException> { Period(date, date) }
    assertThrows<IllegalArgumentException> { Period(date.plusDays(1), date) }
  }

  @Test
  fun `should create validity correctly`() {
    val validity = Period(
      start = LocalDate.of(2024, 10, 1),
      end = LocalDate.of(2024, 10, 31),
    )
    assertTrue(validity.isValid(LocalDate.of(2024, 10, 7)))
  }

  @Test
  fun `throw exception when start date is after end date in Period`() {
    assertThrows<IllegalArgumentException> {
      Period(start = LocalDate.of(2024, 12, 31), end = LocalDate.of(2024, 1, 1))
    }
  }

  @Test
  fun `Yearly validity wrap around September to May`() {
    val validity = Validity.Yearly(
      start = MonthDay.of(Month.SEPTEMBER, 1),
      end = MonthDay.of(Month.MAY, 31),
    )

    assertTrue(validity.isValid(LocalDate.of(2024, 10, 15))) // Oct is valid
    assertTrue(validity.isValid(LocalDate.of(2025, 2, 10))) // Feb is valid
    assertFalse(validity.isValid(LocalDate.of(2024, 7, 10))) // July is invalid
  }

  @Test
  fun `should create yearly successfully`() {
    val start = MonthDay.of(1, 1)
    val end = MonthDay.of(12, 31)
    val yearly = Validity.Yearly(start, end)

    assertEquals(start, yearly.start)
    assertEquals(end, yearly.end)
  }

  @Test
  fun `should create from successfully`() {
    val start = LocalDate.now()
    val from = Validity.From(start)

    assertEquals(start, from.start)
  }

  @Test
  fun `should create until successfully`() {
    val end = LocalDate.now()
    val until = Validity.Until(end)

    assertEquals(end, until.end)
  }

  @Test
  fun `should create always successfully`() {
    val always = Validity.Always
    assertTrue(always is Validity.Always)
  }
}
