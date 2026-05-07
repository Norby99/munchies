package com.munchies.restaurant.application.usecases.menu

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItem
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

class RemoveMenuItemTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var removeMenuItemUseCase: RemoveMenuItemUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    removeMenuItemUseCase = RemoveMenuItemUseCase(menuRepository)
  }

  @Test
  fun `Should remove menu item successfully when menu and category exist`() = runBlocking {
    val itemId = MenuItemId()
    val item =
      MenuItem(
        id = itemId,
        name = MenuItemName.of("Pasta"),
        description = MenuItemDescription.of("Good pasta"),
        price = Money(BigDecimal("12.00")),
      )
    val categoryId = CategoryId()
    val category =
      spyk(Category(id = categoryId, name = CategoryName.of("Mains"), items = mutableListOf(item)))
    val menuId = MenuId()
    val menu =
      spyk(
        Menu(id = menuId, restaurantId = RestaurantId(), categories = listOf(category)),
      )

    val command = RemoveMenuItemCommand(
      menuId = menuId.value,
      categoryId = categoryId.value,
      itemId = itemId.value,
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = removeMenuItemUseCase(command)) {
      is RemoveMenuItemResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assertEquals(0, category.items.size)
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command = RemoveMenuItemCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = removeMenuItemUseCase(command)) {
      is RemoveMenuItemResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail when category does not exist`() = runBlocking {
    val menuId = MenuId()
    val menu = Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList())

    val command = RemoveMenuItemCommand(
      menuId = menuId.value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = removeMenuItemUseCase(command)) {
      is RemoveMenuItemResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
