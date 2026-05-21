package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Category
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

class AddMenuItemTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var addMenuItemUseCase: AddMenuItemUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    addMenuItemUseCase = AddMenuItemUseCase(menuRepository)
  }

  @Test
  fun `should add menu item successfully when menu and category exist`() = runBlocking {
    val categoryId = CategoryId()
    val category = spyk(Category(id = categoryId, name = CategoryName.of("Starters")))
    val menuId = MenuId()
    val menu =
      spyk(
        Menu(id = menuId, restaurantId = RestaurantId(), categories = listOf(category)),
      )

    val command = AddMenuItemCommand(
      menuId = menuId.value,
      categoryId = categoryId.value,
      name = "Salad",
      description = "Fresh green salad",
      price = BigDecimal("5.50"),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addMenuItemUseCase(command)) {
      is AddMenuItemResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(1, category.items.size)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = AddMenuItemCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      name = "Salad",
      description = "Fresh green salad",
      price = BigDecimal("5.50"),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = addMenuItemUseCase(command)) {
      is AddMenuItemResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when category does not exist`() = runBlocking {
    val menuId = MenuId()
    val menu = Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList())

    val command = AddMenuItemCommand(
      menuId = menuId.value,
      categoryId = CategoryId().value,
      name = "Salad",
      description = "Fresh green salad",
      price = BigDecimal("5.50"),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addMenuItemUseCase(command)) {
      is AddMenuItemResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
