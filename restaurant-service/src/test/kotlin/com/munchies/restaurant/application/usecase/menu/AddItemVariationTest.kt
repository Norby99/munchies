package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddItemVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var addItemVariationUseCase: AddItemVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    addItemVariationUseCase = AddItemVariationUseCase(menuRepository)
  }

  @Test
  fun `should add variation to specific item successfully`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))
    val item = category.addItem(
      MenuItemName.of("Pizza Crudo"),
      MenuItemDescription.of("The crudo pizza"),
      Money(BigDecimal("8.50")),
    )

    val command = AddItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      name = "Crudo prep",
      options = listOf(VariationOptionDto("Post-bake", BigDecimal("0.0"))),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addItemVariationUseCase(command)) {
      is AddItemVariationResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(1, item.variations.size)
        assertEquals("Crudo prep", item.variations[0].name.value)
        assertEquals(1, item.variations[0].options.size)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = AddItemVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = addItemVariationUseCase(command)) {
      is AddItemVariationResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when category does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))

    val command = AddItemVariationCommand(
      menuId = menu.id.value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addItemVariationUseCase(command)) {
      is AddItemVariationResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected CategoryNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when item does not exist`() = runBlocking {
    val menu = spyk(Menu.create(restaurantId = RestaurantId()))
    val category = menu.addCategory(CategoryName.of("Pizzas"))

    val command = AddItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = MenuItemId().value,
      name = "Dough",
      options = emptyList(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addItemVariationUseCase(command)) {
      is AddItemVariationResult.ItemNotFound -> {
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

    val command = AddItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      name = "   ",
      options = listOf(),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addItemVariationUseCase(command)) {
      is AddItemVariationResult.InvalidVariation -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected InvalidVariation, but got $result" }
      }
    }
  }
}
