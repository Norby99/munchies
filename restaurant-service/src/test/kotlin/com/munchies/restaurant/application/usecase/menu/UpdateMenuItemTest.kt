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
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateMenuItemTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var updateMenuItemUseCase: UpdateMenuItemUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    updateMenuItemUseCase = UpdateMenuItemUseCase(menuRepository)
  }

  @Test
  fun `should update menu item successfully when menu and category exist`(): Unit = runBlocking {
    val item =
      MenuItem(
        id = MenuItemId(),
        details = MenuItemDetails(
          MenuItemName.of("Old Name"),
          MenuItemDescription.of("Old Desc"),
        ),
        price = Money(BigDecimal("10.0")),
        variations = emptyList(),
      )
    val category = spyk(
      Category(
        id = CategoryId(),
        name = CategoryName.of("Mains"),
        items = mutableListOf(item),
      ),
    )
    val menu = spyk(
      Menu(
        id = MenuId(),
        restaurantId = RestaurantId(),
        categories = listOf(category),
      ),
    )

    val variations = listOf(
      VariationDto(
        "Size",
        listOf(
          VariationOptionDto("Normal", BigDecimal("0.00")),
          VariationOptionDto("Large", BigDecimal("2.00")),
        ),
      ),
    )
    val command = UpdateMenuItemCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      name = "New Name",
      description = "New Desc",
      price = BigDecimal("15.00"),
      variations = variations,
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = updateMenuItemUseCase(command)) {
      is UpdateMenuItemResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        item.name.value shouldBe "New Name"
        item.description.value shouldBe "New Desc"
        item.price.amount shouldBe BigDecimal("15.00")
        item.variations shouldBe variations.map { it.toDomain() }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = UpdateMenuItemCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      name = "New Name",
      description = "New Desc",
      price = BigDecimal("15.00"),
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = updateMenuItemUseCase(command)) {
      is UpdateMenuItemResult.MenuNotFound -> {
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

    val command = UpdateMenuItemCommand(
      menuId = menuId.value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      name = "New Name",
      description = "New Desc",
      price = BigDecimal("15.00"),
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateMenuItemUseCase(command)) {
      is UpdateMenuItemResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
