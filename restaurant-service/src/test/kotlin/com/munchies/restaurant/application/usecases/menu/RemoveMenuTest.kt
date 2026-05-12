package com.munchies.restaurant.application.usecases.menu

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
  private lateinit var removeMenuUseCase: RemoveMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    removeMenuUseCase = RemoveMenuUseCase(menuRepository)
  }

  @Test
  fun `Should remove menu successfully when menu exists`() = runBlocking {
    val menu = spyk(Menu.create(RestaurantId(), MenuName.of("Main Menu"), Validity.always))
    val menuId = menu.id
    val command = RemoveMenuCommand(menuId.value)

    coEvery { menuRepository.findById(any()) } returns menu
    coEvery { menuRepository.delete(any()) } returns Unit

    when (val result = removeMenuUseCase(command)) {
      is RemoveMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.delete(menuId) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `Should fail when menu does not exist`() = runBlocking {
    val command = RemoveMenuCommand(MenuId().value)

    coEvery { menuRepository.findById(any()) } returns null

    when (val result = removeMenuUseCase(command)) {
      is RemoveMenuResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.delete(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }
}
