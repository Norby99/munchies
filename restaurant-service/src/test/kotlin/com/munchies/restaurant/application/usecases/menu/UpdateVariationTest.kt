package com.munchies.restaurant.application.usecases.menu

import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var updateVariationUseCase: UpdateVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    updateVariationUseCase = UpdateVariationUseCase(menuRepository)
  }

  @Test
  fun `Should update variation of category successfully`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))
    val variation = category.addVariation(
      VariationName.of("Dough Type"),
      listOf(VariationOption(VariationOptionName.of("Regular"), Money(BigDecimal("0.0")))),
    )

    val command = UpdateVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      variationId = variation.id.value,
      name = "Dough Special",
      options = listOf(VariationOptionDto("Gluten Free", BigDecimal("2.50"))),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = updateVariationUseCase(command)) {
      is UpdateVariationResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        val updatedVariation = category.variations.first { it.id == variation.id }
        assertEquals("Dough Special", updatedVariation.name.value)
        assertEquals(1, updatedVariation.options.size)
        assertEquals("Gluten Free", updatedVariation.options[0].name.value)
        assertEquals(BigDecimal("2.50"), updatedVariation.options[0].additionalPrice.amount)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command = UpdateVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      variationId = VariationId().value,
      name = "Dough Special",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = updateVariationUseCase(command)) {
      is UpdateVariationResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `Should fail when category does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))

    val command = UpdateVariationCommand(
      menuId = menu.id.value,
      categoryId = CategoryId().value,
      variationId = VariationId().value,
      name = "Dough Special",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateVariationUseCase(command)) {
      is UpdateVariationResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected CategoryNotFound, but got $result" }
    }
  }

  @Test
  fun `Should fail when variation data is invalid`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))
    val variation = category.addVariation(
      VariationName.of("Dough Type"),
      listOf(VariationOption(VariationOptionName.of("Regular"), Money(BigDecimal("0.0")))),
    )

    val command = UpdateVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      variationId = variation.id.value,
      name = "   ",
      options = listOf(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateVariationUseCase(command)) {
      is UpdateVariationResult.InvalidVariation -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected InvalidVariation, but got $result" }
      }
    }
  }
}
