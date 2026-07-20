package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var updateMenuUseCase: UpdateMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    updateMenuUseCase = UpdateMenuUseCase(menuRepository)
  }

  @Test
  fun `should update menu successfully when menu exists`() = runBlocking {
    val menu =
      spyk(
        Menu.create(
          RestaurantId(),
          MenuName.of("Old Name"),
          com.munchies.restaurant.domain.valueobject.menu.Validity.always,
        ),
      )

    val command = UpdateMenuCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menu.id.value,
      name = "Spring Menu",
      validity = ValidityInput.Period("2027-3-1", "2027-5-31"),
    )

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.save(any()) } returns Unit

    when (val result = updateMenuUseCase(command)) {
      is UpdateMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.save(menu) }
        assert(menu.name.value == "Spring Menu")
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = UpdateMenuCommand(
      restaurantId = RestaurantId().value,
      menuId = MenuId().value,
      name = "Spring Menu",
      validity = ValidityInput.Always,
    )

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = updateMenuUseCase(command)) {
      is UpdateMenuResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when menu name is invalid`() = runBlocking {
    val menu =
      spyk(
        Menu.create(
          RestaurantId(),
          MenuName.of("Old Name"),
          com.munchies.restaurant.domain.valueobject.menu.Validity.always,
        ),
      )

    val command = UpdateMenuCommand(
      restaurantId = menu.restaurantId.value,
      menuId = menu.id.value,
      name = "   ",
      validity = ValidityInput.Always,
    )

    coEvery { menuRepository.findById(any()) } returns menu

    when (val result = updateMenuUseCase(command)) {
      is UpdateMenuResult.InvalidMenu -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected InvalidMenu, but got $result" }
    }
  }
}
