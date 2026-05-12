package com.munchies.restaurant.application.usecases.menu

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
  private lateinit var addCategoryUseCase: AddCategoryUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    addCategoryUseCase = AddCategoryUseCase(menuRepository)
  }

  @Test
  fun `Should add category successfully when menu exists`() = runBlocking {
    val menuId = MenuId()
    val menu =
      spyk(Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList()))

    val command = AddCategoryCommand(menuId.value, "Desserts")

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addCategoryUseCase(command)) {
      is AddCategoryResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  fun `Should fail with Error when menu does not exist`() = runBlocking {
    val command = AddCategoryCommand(MenuId().value, "Desserts")

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = addCategoryUseCase(command)) {
      is AddCategoryResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }

  @Test
  fun `Should map IllegalArgumentException to Error`() = runBlocking {
    val menuId = MenuId()
    val menu =
      spyk(Menu(id = menuId, restaurantId = RestaurantId(), categories = emptyList()))

    val command = AddCategoryCommand(menuId.value, "    ")

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = addCategoryUseCase(command)) {
      is AddCategoryResult.InvalidCategory -> {
        assertTrue(result.error.isNotEmpty())
      }
      else -> {
        assert(false) { "Expected Error, but got $result" }
      }
    }
  }
}
