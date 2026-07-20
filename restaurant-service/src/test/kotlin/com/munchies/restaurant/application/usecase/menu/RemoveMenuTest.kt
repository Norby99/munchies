package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var deleteMenuUseCase: DeleteMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    deleteMenuUseCase = DeleteMenuUseCase(menuRepository)
  }

  @Test
  fun `should remove menu successfully when menu exists`() = runBlocking {
    val menu = spyk(
      Menu.create(
        restaurantId = RestaurantId(),
        name = MenuName.of("Main Menu"),
        validity = Validity.always,
      ),
    )
    val menuId = menu.id
    val command = DeleteMenuCommand(menu.restaurantId.value, menuId.value)

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.delete(any()) } returns Unit

    when (val result = deleteMenuUseCase(command)) {
      is DeleteMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.delete(menuId) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = DeleteMenuCommand(RestaurantId().value, MenuId().value)

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = deleteMenuUseCase(command)) {
      is DeleteMenuResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.delete(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }
}
