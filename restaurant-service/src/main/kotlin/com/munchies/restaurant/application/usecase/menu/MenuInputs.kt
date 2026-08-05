package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.usecase.menu.ValidityInput.Always
import com.munchies.restaurant.application.usecase.menu.ValidityInput.And
import com.munchies.restaurant.application.usecase.menu.ValidityInput.From
import com.munchies.restaurant.application.usecase.menu.ValidityInput.Hours
import com.munchies.restaurant.application.usecase.menu.ValidityInput.Period
import com.munchies.restaurant.application.usecase.menu.ValidityInput.Until
import com.munchies.restaurant.application.usecase.menu.ValidityInput.Weekly
import com.munchies.restaurant.application.usecase.menu.ValidityInput.Yearly
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

sealed interface ValidityInput {
  data class Period(val start: String, val end: String) : ValidityInput
  data class Yearly(
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
  ) : ValidityInput
  data class Weekly(val days: List<Int>) : ValidityInput
  data class From(val start: String) : ValidityInput
  data class Until(val end: String) : ValidityInput
  data object Always : ValidityInput
  data class Hours(val start: String, val end: String) : ValidityInput
  data class And(val first: ValidityInput, val second: ValidityInput) : ValidityInput

  fun toDomain(): Validity = when (this) {
    is Period -> Validity.period(start, end)
    is Yearly -> Validity.yearly(startMonth, startDay, endMonth, endDay)
    is Weekly -> Validity.weekly(days)
    is From -> Validity.from(start)
    is Until -> Validity.until(end)
    is Always -> Validity.always
    is Hours -> Validity.hours(LocalTime.parse(start), LocalTime.parse(end))
    is And -> first.toDomain().combine(second.toDomain())
  }
}

fun Validity.toInput(): ValidityInput = when (this) {
  is Validity.Period -> {
    if (end == LocalDate.MAX) {
      From(start.toString())
    } else if (start == LocalDate.MIN) {
      Until(end.toString())
    } else {
      Period(start.toString(), end.toString())
    }
  }
  is Validity.Yearly -> Yearly(
    start.monthValue,
    start.dayOfMonth,
    end.monthValue,
    end.dayOfMonth,
  )
  is Validity.Weekly -> Weekly(days.map { it.value })
  is Validity.Always -> Always
  is Validity.Hours -> Hours(start.toString(), end.toString())
  is Validity.And -> And(first.toInput(), (second.toInput()))
}

data class VariationOptionInput(val name: String, val additionalPrice: BigDecimal) {
  fun toDomain(): VariationOption = VariationOption(name, Money(additionalPrice))
}

data class VariationInput(val name: String, val options: List<VariationOptionInput>) {
  fun toDomain(): Variation = Variation(VariationName(name), options.map { it.toDomain() })
}

fun VariationOption.toInput(): VariationOptionInput =
  VariationOptionInput(name, additionalPrice.amount)

fun Variation.toInput(): VariationInput = VariationInput(name.value, options.map { it.toInput() })
