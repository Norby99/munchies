package com.munchies.restaurant.domain.valueobject.menu

import com.munchies.restaurant.domain.valueobject.Money
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.Test

class VariationTests {

  @Test
  fun `should create variation successfully`() {
    val variation = Variation(VariationName("Size"))
      .option("Small", Money(0))
      .option("Medium", Money((2)))
      .option("Large", Money(4))

    variation.name.value shouldBe "Size"
    variation.options.size shouldBe 3
    variation.options shouldExist { it.name == "Small" && it.additionalPrice == Money(0) }
    variation.options shouldExist { it.name == "Medium" && it.additionalPrice == Money(2) }
    variation.options shouldExist { it.name == "Large" && it.additionalPrice == Money(4) }
  }
}
