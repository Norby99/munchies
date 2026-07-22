package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
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
  private lateinit var deleteMenuItemUseCase: DeleteMenuItemUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    deleteMenuItemUseCase = DeleteMenuItemUseCase(menuRepository)
  }

  @Test
  fun `should delete menu item successfully when menu and category exist`() = runBlocking {
    val item =
      MenuItem(
        id = MenuItemId(),
        details = MenuItemDetails(
          MenuItemName.of("Pasta"),
          MenuItemDescription.of("Good pasta"),
        ),
        price = Money(BigDecimal("12.00")),
      )
    val category =
      spyk(
        Category(
          id = CategoryId(),
          name = CategoryName.of("Mains"),
          items = mutableListOf(item),
        ),
      )
    val menu =
      spyk(
        Menu(
          id = MenuId(),
          restaurantId = RestaurantId(),
          categories = listOf(category),
        ),
      )

    val command = DeleteMenuItemCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = deleteMenuItemUseCase(command)) {
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
  fun `should fail when menu does not exist`() = runBlocking {
    val command = DeleteMenuItemCommand(
      restaurantId = RestaurantId().value,
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns null

    when (val result = deleteMenuItemUseCase(command)) {
      is RemoveMenuItemResult.MenuNotFound -> {
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

    val command = DeleteMenuItemCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menuId.value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu

    when (val result = deleteMenuItemUseCase(command)) {
      is RemoveMenuItemResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
