package com.munchies.restaurant.domain.entity

import com.munchies.restaurant.domain.aggregate.Variation
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VariationTest {
  @Test
  fun `should update variation details correctly`() {
    val variation = Variation(
      name = VariationName.of("Size"),
      options = listOf(
        VariationOption(
          VariationOptionName.of("Small"),
          Money(BigDecimal("0.0")),
        ),
      ),
    )

    val newName = VariationName.of("Crust")
    val newOptions = listOf(
      VariationOption(
        VariationOptionName.of("Thin"),
        Money(BigDecimal("2.0")),
      ),
    )

    variation.update(newName, newOptions)

    assertEquals(newName, variation.name)
    assertEquals(newOptions, variation.options)
  }
}
