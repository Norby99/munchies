package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import java.math.BigDecimal
import java.time.LocalDate
import java.time.MonthDay

sealed interface ValidityConfig {
  data class Period(val start: LocalDate, val end: LocalDate) : ValidityConfig
  data class Yearly(
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
  ) : ValidityConfig
  data class From(val start: LocalDate) : ValidityConfig
  data class Until(val end: LocalDate) : ValidityConfig
  data object Always : ValidityConfig

  fun toDomain(): Validity = when (this) {
    is Period -> Validity.period(start, end)
    is Yearly -> Validity.yearly(MonthDay.of(startMonth, startDay), MonthDay.of(endMonth, endDay))
    is From -> Validity.from(start)
    is Until -> Validity.until(end)
    is Always -> Validity.always
  }
}

data class VariationOptionDto(val name: String, val additionalPrice: BigDecimal) {
  constructor(option: VariationOption) : this(option.name, option.additionalPrice.amount)

  fun toDomain(): VariationOption = VariationOption(name, Money(additionalPrice))
}

data class VariationDto(val name: String, val options: List<VariationOptionDto>) {
  constructor(
    variation: Variation,
  ) : this(variation.name.value, variation.options.map(::VariationOptionDto))

  fun toDomain(): Variation = Variation(VariationName(name), options.map { it.toDomain() })
}
