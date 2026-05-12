package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var addVariationUseCase: AddVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    addVariationUseCase = AddVariationUseCase(menuRepository)
  }

  @Test
  fun `should add variation to category successfully`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))

    val command = AddVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      name = "Dough Type",
      options = listOf(VariationOptionDto("Gluten Free", BigDecimal("2.50"))),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addVariationUseCase(command)) {
      is AddVariationResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(1, category.variations.size)
        assertEquals("Dough Type", category.variations[0].name.value)
        assertEquals(1, category.variations[0].options.size)
        assertEquals("Gluten Free", category.variations[0].options[0].name.value)
        assertEquals(BigDecimal("2.50"), category.variations[0].options[0].additionalPrice.amount)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = AddVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      name = "Dough Type",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = addVariationUseCase(command)) {
      is AddVariationResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when category does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))

    val command = AddVariationCommand(
      menuId = menu.id.value,
      categoryId = CategoryId().value,
      name = "Dough Type",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addVariationUseCase(command)) {
      is AddVariationResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected CategoryNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when variation data is invalid`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))

    val command = AddVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      name = "   ",
      options = listOf(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addVariationUseCase(command)) {
      is AddVariationResult.InvalidVariation -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected InvalidVariation, but got $result" }
      }
    }
  }
}
