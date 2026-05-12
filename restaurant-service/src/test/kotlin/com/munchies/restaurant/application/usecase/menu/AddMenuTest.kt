package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var addMenuUseCase: AddMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    addMenuUseCase = AddMenuUseCase(menuRepository)
  }

  @Test
  fun `should add menu successfully with valid data`() = runBlocking {
    val command = AddMenuCommand(
      restaurantId = RestaurantId().value,
      name = "Winter Menu",
      validity = ValidityConfig.Period(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 2, 28)),
    )

    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addMenuUseCase(command)) {
      is AddMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should add menu successfully without validity dates`() = runBlocking {
    val command = AddMenuCommand(
      restaurantId = RestaurantId().value,
      name = "Main Menu",
      validity = ValidityConfig.Always,
    )

    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = addMenuUseCase(command)) {
      is AddMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should fail when menu name is blank`() = runBlocking {
    val command = AddMenuCommand(
      restaurantId = RestaurantId().value,
      name = "   ",
      validity = ValidityConfig.Always,
    )

    when (val result = addMenuUseCase(command)) {
      is AddMenuResult.InvalidMenu -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected InvalidMenu, but got $result" }
    }
  }
}
