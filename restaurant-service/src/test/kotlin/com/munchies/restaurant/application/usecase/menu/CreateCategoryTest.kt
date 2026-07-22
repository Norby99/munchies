package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateCategoryTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var createCategoryUseCase: CreateCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    createCategoryUseCase = CreateCategoryUseCase(menuRepository)
  }

  @Test
  fun `should create category successfully when menu exists`() = runBlocking {
    val menu =
      spyk(Menu(id = MenuId(), restaurantId = RestaurantId(), categories = emptyList()))

    val command = CreateCategoryCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menu.id.value,
      name = "Desserts",
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = createCategoryUseCase(command)) {
      is CreateCategoryResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `should fail with Error when menu does not exist`() = runBlocking {
    val command = CreateCategoryCommand(
      restaurantId = RestaurantId().value,
      menuId = MenuId().value,
      name = "Desserts",
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns null

    when (val result = createCategoryUseCase(command)) {
      is CreateCategoryResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }

  @Test
  fun `should map IllegalArgumentException to Error`() = runBlocking {
    val menuId = MenuId()
    val menu =
      spyk(Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList()))

    val command = CreateCategoryCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menuId.value,
      name = "    ",
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu

    when (val result = createCategoryUseCase(command)) {
      is CreateCategoryResult.InvalidCategory -> {
        assertTrue(result.error.isNotEmpty())
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
