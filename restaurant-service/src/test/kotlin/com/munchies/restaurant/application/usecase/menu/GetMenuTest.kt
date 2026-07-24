package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var getMenuUseCase: GetMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    getMenuUseCase = GetMenuUseCase(menuRepository)
  }

  @Test
  fun `should return Success with menu when menu exists`(): Unit = runBlocking {
    val menu = Menu.create(
      restaurantId = RestaurantId(),
      name = MenuName.of("Winter Menu"),
      validity = Validity.period("2026-12-01", "2027-02-28"),
    )

    val command = GetMenuCommand(menu.restaurantId.value, menu.id.value)
    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu

    val result = getMenuUseCase(command)

    result shouldBe GetMenuResult.Success(menu)
  }

  @Test
  fun `should return MenuNotFound when menu does not exist`(): Unit = runBlocking {
    val command = GetMenuCommand(RestaurantId().value, MenuId().value)
    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns null

    val result = getMenuUseCase(command)

    result shouldBe GetMenuResult.MenuNotFound
  }

  @Test
  fun `should return Success with all menu properties correctly`(): Unit = runBlocking {
    val menu = Menu.create(
      restaurantId = RestaurantId(),
      name = MenuName.of("Spring Menu"),
      validity = Validity.period("2027-03-01", "2027-05-31"),
    )

    val command = GetMenuCommand(menu.restaurantId.value, menu.id.value)
    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu

    val successResult = getMenuUseCase(command) as? GetMenuResult.Success

    successResult?.menu shouldBe menu
  }
}
