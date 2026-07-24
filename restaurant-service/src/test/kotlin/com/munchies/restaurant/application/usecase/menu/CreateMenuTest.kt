package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var createMenuUseCase: CreateMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    createMenuUseCase = CreateMenuUseCase(menuRepository)
  }

  @Test
  fun `should add menu successfully with valid data`() = runBlocking {
    val command = CreateMenuCommand(
      restaurantId = RestaurantId().value,
      name = "Winter Menu",
      validity = ValidityInput.Period("2026-12-01", "2027-02-28"),
    )

    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should add menu successfully without validity dates`() = runBlocking {
    val command = CreateMenuCommand(
      restaurantId = RestaurantId().value,
      name = "Main Menu",
      validity = ValidityInput.Always,
    )

    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should fail when menu name is blank`() = runBlocking {
    val command = CreateMenuCommand(
      restaurantId = RestaurantId().value,
      name = "   ",
      validity = ValidityInput.Always,
    )

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.InvalidMenu -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected InvalidMenu, but got $result" }
    }
  }
}
