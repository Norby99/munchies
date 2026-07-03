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

class AddCategoryTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var createCategoryUseCase: CreateCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    createCategoryUseCase = CreateCategoryUseCase(menuRepository)
  }

  @Test
  fun `should add category successfully when menu exists`() = runBlocking {
    val menuId = MenuId()
    val menu =
      spyk(Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList()))

    val command = CreateCategoryCommand(menuId.value, "Desserts")

    coEvery { menuRepository.findById(any()) } returns menu
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
    val command = CreateCategoryCommand(MenuId().value, "Desserts")

    coEvery { menuRepository.findById(any()) } returns null

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

    val command = CreateCategoryCommand(menuId.value, "    ")

    coEvery { menuRepository.findById(any()) } returns menu

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
