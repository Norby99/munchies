package com.munchies.restaurant.application.usecases.menu

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.Variation
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveVariationTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var removeVariationUseCase: RemoveVariationUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    removeVariationUseCase = RemoveVariationUseCase(menuRepository)
  }

  @Test
  fun `Should remove variation successfully when menu and category and variation exist`() =
    runBlocking {
      val variationId = VariationId()
      val variation =
        spyk(
          Variation(id = variationId, name = VariationName.of("Dough"), options = mutableListOf()),
        )
      val categoryId = CategoryId()
      val category = spyk(
        Category(
          id = categoryId,
          name = CategoryName.of("Pizza"),
          variations = mutableListOf(variation),
        ),
      )
      val menuId = MenuId()
      val menu =
        spyk(
          Menu(id = menuId, restaurantId = RestaurantId(), categories = listOf(category)),
        )

      val command = RemoveVariationCommand(
        menuId = menuId.value,
        categoryId = categoryId.value,
        variationId = variationId.value,
      )

      coEvery { menuRepository.findById(any()) } returns menu
      coEvery { menuRepository.save(any()) } returns Unit

      when (val result = removeVariationUseCase(command)) {
        is RemoveVariationResult.Success -> {
          coVerify(exactly = 1) { menuRepository.save(menu) }
          assertEquals(0, category.variations.size)
        }
        else -> {
          assert(false) { "Expected Success, but got $result" }
        }
      }
    }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command = RemoveVariationCommand(
      menuId = MenuId().value,
      categoryId = CategoryId().value,
      variationId = VariationId().value,
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = removeVariationUseCase(command)) {
      is RemoveVariationResult.MenuNotFound -> {
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

    val command = RemoveVariationCommand(
      menuId = menuId.value,
      categoryId = CategoryId().value,
      variationId = VariationId().value,
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = removeVariationUseCase(command)) {
      is RemoveVariationResult.CategoryNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
