package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
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

class UpdateItemVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var updateItemVariationUseCase: UpdateItemVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    updateItemVariationUseCase = UpdateItemVariationUseCase(menuRepository)
  }

  @Test
  fun `should update variation of specific item successfully`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))
    val item = category.addItem(
      MenuItemName.of("Pizza Crudo"),
      MenuItemDescription.of("The crudo pizza"),
      Money(BigDecimal("8.50")),
    )
    val variation = item.addVariation(
      VariationName.of("Crudo prep"),
      listOf(VariationOption(VariationOptionName.of("Post-bake"), Money(BigDecimal("0.0")))),
    )

    val command = UpdateItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      variationId = variation.id.value,
      name = "Crudo Base",
      options = listOf(VariationOptionDto("Pre-bake", BigDecimal("1.0"))),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = updateItemVariationUseCase(command)) {
      is UpdateItemVariationResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        val updatedVariation = item.variations.first { it.id == variation.id }
        assertEquals("Crudo Base", updatedVariation.name.value)
        assertEquals(1, updatedVariation.options.size)
        assertEquals("Pre-bake", updatedVariation.options[0].name.value)
        assertEquals(BigDecimal("1.0"), updatedVariation.options[0].additionalPrice.amount)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = UpdateItemVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      variationId = VariationId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = updateItemVariationUseCase(command)) {
      is UpdateItemVariationResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when category does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))

    val command = UpdateItemVariationCommand(
      menuId = menu.id.value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      variationId = VariationId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateItemVariationUseCase(command)) {
      is UpdateItemVariationResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected CategoryNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when item does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))

    val command = UpdateItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = MenuItemId().value,
      variationId = VariationId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateItemVariationUseCase(command)) {
      is UpdateItemVariationResult.ItemNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected ItemNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when variation data is invalid`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))
    val item = category.addItem(
      MenuItemName.of("Pizza Crudo"),
      MenuItemDescription.of("The crudo pizza"),
      Money(BigDecimal("8.50")),
    )
    val variation = item.addVariation(
      VariationName.of("Crudo prep"),
      listOf(VariationOption(VariationOptionName.of("Post-bake"), Money(BigDecimal("0.0")))),
    )

    val command = UpdateItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      variationId = variation.id.value,
      name = "   ",
      options = listOf(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateItemVariationUseCase(command)) {
      is UpdateItemVariationResult.InvalidVariation -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected InvalidVariation, but got $result" }
      }
    }
  }
}
