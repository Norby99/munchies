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
import java.time.LocalDate
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
    val restaurantId = RestaurantId()
    val menuId = MenuId()
    val expectedMenu = Menu.create(
      restaurantId = restaurantId,
      name = MenuName.of("Winter Menu"),
      validity = Validity.period(
        LocalDate.of(2026, 12, 1),
        LocalDate.of(2027, 2, 28),
      ),
    )

    val command = GetMenuCommand(menuId.value)
    coEvery { menuRepository.findById(menuId) } returns expectedMenu

    val result = getMenuUseCase(command)

    result shouldBe GetMenuResult.Success(expectedMenu)
  }

  @Test
  fun `should return MenuNotFound when menu does not exist`(): Unit = runBlocking {
    val menuId = MenuId()
    val command = GetMenuCommand(menuId.value)
    coEvery { menuRepository.findById(menuId) } returns null

    val result = getMenuUseCase(command)

    result shouldBe GetMenuResult.MenuNotFound
  }

  @Test
  fun `should return Success with all menu properties correctly`(): Unit = runBlocking {
    val restaurantId = RestaurantId()
    val menuId = MenuId()
    val menuName = "Spring Menu"
    val startDate = LocalDate.of(2027, 3, 1)
    val endDate = LocalDate.of(2027, 5, 31)

    val menu = Menu.create(
      restaurantId = restaurantId,
      name = MenuName.of(menuName),
      validity = Validity.period(startDate, endDate),
    )

    val command = GetMenuCommand(menuId.value)
    coEvery { menuRepository.findById(menuId) } returns menu

    val successResult = getMenuUseCase(command) as? GetMenuResult.Success

    successResult?.menu shouldBe menu
  }
}
