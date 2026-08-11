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
import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuItemDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuSummaryDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationOptionDto
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

fun Array<ValidityDto>.toInput(): ValidityInput {
  require(isNotEmpty()) { "validity must contain at least one rule" }
  return map { it.toSingleInput() }.reduce { acc, next -> ValidityInput.And(acc, next) }
}

private fun ValidityDto.toSingleInput(): ValidityInput = when (type) {
  ValidityType.PERIOD -> ValidityInput.Period(value.getValue("start"), value.getValue("end"))
  ValidityType.YEARLY -> ValidityInput.Yearly(
    value.getValue("startMonth").toInt(),
    value.getValue("startDay").toInt(),
    value.getValue("endMonth").toInt(),
    value.getValue("endDay").toInt(),
  )
  ValidityType.WEEKLY -> ValidityInput.Weekly(
    value.getValue("days").split(",").filter { it.isNotEmpty() }.map { it.toInt() },
  )
  ValidityType.HOURS -> ValidityInput.Hours(value.getValue("start"), value.getValue("end"))
  ValidityType.FROM -> ValidityInput.From(value.getValue("start"))
  ValidityType.UNTIL -> ValidityInput.Until(value.getValue("end"))
  ValidityType.ALWAYS -> ValidityInput.Always
  else -> error("Unknown validity type: $type")
}

fun ValidityInput.toDto(): Array<ValidityDto> = when (this) {
  is ValidityInput.And -> first.toDto() + second.toDto()
  else -> arrayOf(toSingleDto())
}

private fun ValidityInput.toSingleDto(): ValidityDto = when (this) {
  is ValidityInput.Period -> ValidityDto(ValidityType.PERIOD, mapOf("start" to start, "end" to end))
  is ValidityInput.Yearly -> ValidityDto(
    ValidityType.YEARLY,
    mapOf(
      "startMonth" to startMonth.toString(),
      "startDay" to startDay.toString(),
      "endMonth" to endMonth.toString(),
      "endDay" to endDay.toString(),
    ),
  )
  is ValidityInput.Weekly -> ValidityDto(ValidityType.WEEKLY, mapOf("days" to days.joinToString(",")))
  is ValidityInput.Hours -> ValidityDto(ValidityType.HOURS, mapOf("start" to start, "end" to end))
  is ValidityInput.From -> ValidityDto(ValidityType.FROM, mapOf("start" to start))
  is ValidityInput.Until -> ValidityDto(ValidityType.UNTIL, mapOf("end" to end))
  is ValidityInput.Always -> ValidityDto(ValidityType.ALWAYS)
  is ValidityInput.And -> error("AND is flattened by toDto() before reaching toSingleDto()")
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
