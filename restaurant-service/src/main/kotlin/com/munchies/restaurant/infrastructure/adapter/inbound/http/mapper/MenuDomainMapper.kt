package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.application.usecase.menu.VariationInput
import com.munchies.restaurant.application.usecase.menu.VariationOptionInput
import com.munchies.restaurant.application.usecase.menu.toInput
import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import com.munchies.restaurant.infrastructure.adapter.dto.AlwaysValidity
import com.munchies.restaurant.infrastructure.adapter.dto.AndValidity
import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.FromValidity
import com.munchies.restaurant.infrastructure.adapter.dto.HoursValidity
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuItemDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuSummaryDto
import com.munchies.restaurant.infrastructure.adapter.dto.PeriodValidity
import com.munchies.restaurant.infrastructure.adapter.dto.UntilValidity
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationOptionDto
import com.munchies.restaurant.infrastructure.adapter.dto.WeeklyValidity
import com.munchies.restaurant.infrastructure.adapter.dto.YearlyValidity
import java.math.BigDecimal

// --- Menu ---

fun Menu.toDto(): MenuDto = MenuDto(
  id = id.value,
  name = name.value,
  categories = categories.map { it.toDto() }.toTypedArray(),
  validity = validity.toInput().toDto(),
)

fun Menu.toSummaryDto(): MenuSummaryDto = MenuSummaryDto(
  id = id.value,
  name = name.value,
)

fun ValidityDto.toInput(): ValidityInput = when (this) {
  is PeriodValidity -> ValidityInput.Period(start, end)
  is YearlyValidity -> ValidityInput.Yearly(startMonth, startDay, endMonth, endDay)
  is WeeklyValidity -> ValidityInput.Weekly(days.toList())
  is FromValidity -> ValidityInput.From(start)
  is UntilValidity -> ValidityInput.Until(end)
  is AlwaysValidity -> ValidityInput.Always
  is HoursValidity -> ValidityInput.Hours(start, end)
  is AndValidity -> ValidityInput.And(first.toInput(), second.toInput())
}

fun ValidityInput.toDto(): ValidityDto = when (this) {
  is ValidityInput.Period -> PeriodValidity(start, end)
  is ValidityInput.Yearly -> YearlyValidity(startMonth, startDay, endMonth, endDay)
  is ValidityInput.Weekly -> WeeklyValidity(days.toTypedArray())
  is ValidityInput.From -> FromValidity(start)
  is ValidityInput.Until -> UntilValidity(end)
  is ValidityInput.Always -> AlwaysValidity
  is ValidityInput.Hours -> HoursValidity(start, end)
  is ValidityInput.And -> AndValidity(first.toDto(), second.toDto())
}

// --- Category ---

fun Category.toDto(): CategoryDto = CategoryDto(
  id = id.value,
  name = name.value,
  items = items.map { it.toDto() }.toTypedArray(),
  variations = variations.map { it.toDto() }.toTypedArray(),
)

fun Variation.toDto(): VariationDto = VariationDto(
  name = name.value,
  options = options.map { it.toDto() }.toTypedArray(),
)

fun VariationOption.toDto(): VariationOptionDto = VariationOptionDto(
  name = name,
  additionalPrice = additionalPrice.amount.toPlainString(),
)

fun VariationDto.toInput(): VariationInput = VariationInput(name, options.map { it.toInput() })

fun VariationOptionDto.toInput(): VariationOptionInput =
  VariationOptionInput(name, BigDecimal(additionalPrice))

// --- MenuItem ---

fun MenuItem.toDto(): MenuItemDto = MenuItemDto(
  id = id.value,
  name = name.value,
  description = description.value,
  price = price.amount.toPlainString(),
  variations = variations.map { it.toDto() }.toTypedArray(),
)
