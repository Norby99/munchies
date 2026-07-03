package com.munchies.restaurant.domain.valueobject.menu

import com.munchies.restaurant.domain.valueobject.Money

data class VariationOption(
  val name: String,
  val additionalPrice: Money,
)

data class Variation(
  val name: VariationName,
  val options: List<VariationOption> = emptyList(),
) {
  fun option(name: String, additionalPrice: Money): Variation {
    return Variation(this.name, this.options + VariationOption(name, additionalPrice))
  }
}
