package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

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
