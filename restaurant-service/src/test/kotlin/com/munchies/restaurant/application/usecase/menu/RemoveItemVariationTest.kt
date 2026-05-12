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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveItemVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var removeItemVariationUseCase: RemoveItemVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    removeItemVariationUseCase = RemoveItemVariationUseCase(menuRepository)
  }

  @Test
  fun `should remove variation from specific item successfully`() = runBlocking {
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

    val command = RemoveItemVariationCommand(
      menuId = menu.id.value,
      categoryId = category.id.value,
      itemId = item.id.value,
      variationId = variation.id.value,
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = removeItemVariationUseCase(command)) {
      is RemoveItemVariationResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assert(item.variations.isEmpty())
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = RemoveItemVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      itemId = MenuItemId().value,
      variationId = VariationId().value,
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = removeItemVariationUseCase(command)) {
      is RemoveItemVariationResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }
}
