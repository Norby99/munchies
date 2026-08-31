package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var deleteMenuUseCase: DeleteMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    restaurantRepository = mockk()
    deleteMenuUseCase = DeleteMenuUseCase(menuRepository, restaurantRepository)
  }

  private fun aRestaurant(): Restaurant = Restaurant.create(
    managerId = UserId(),
    name = RestaurantName.of("Trattoria da Piero"),
    address = Address.of("Via Roma 1, Milano"),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("piero@example.com"),
  )

  @Test
  fun `should remove menu successfully when menu exists`() = runBlocking {
    val menu = spyk(
      Menu.create(
        restaurantId = RestaurantId(),
        name = MenuName.of("Main Menu"),
        validity = Validity.always,
      ),
    )
    val restaurant = aRestaurant()
    val command = DeleteMenuCommand(
      menu.restaurantId.value,
      menu.id.value,
      restaurant.managerId.value,
    )

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu
    coEvery { restaurantRepository.findById(menu.restaurantId) } returns restaurant
    coEvery { menuRepository.delete(any()) } returns Unit

    when (val result = deleteMenuUseCase(command)) {
      is DeleteMenuResult.Success -> {
        coVerify(exactly = 1) { menuRepository.delete(menu) }
      }
      else -> assert(false) { "Expected Success, but got $result" }
    }
  }

  @Test
  fun `should fail when menu does not exist`() = runBlocking {
    val command = DeleteMenuCommand(RestaurantId().value, MenuId().value, UserId().value)

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns null

    when (val result = deleteMenuUseCase(command)) {
      is DeleteMenuResult.MenuNotFound -> {
        coVerify(exactly = 0) { menuRepository.delete(any()) }
      }
      else -> assert(false) { "Expected MenuNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when caller is not the owning manager`() = runBlocking {
    val menu = spyk(
      Menu.create(
        restaurantId = RestaurantId(),
        name = MenuName.of("Main Menu"),
        validity = Validity.always,
      ),
    )
    val restaurant = aRestaurant()
    val command = DeleteMenuCommand(menu.restaurantId.value, menu.id.value, UserId().value)

    coEvery { menuRepository.findByIdAndRestaurantId(any(), any()) } returns menu
    coEvery { restaurantRepository.findById(menu.restaurantId) } returns restaurant

    when (val result = deleteMenuUseCase(command)) {
      is DeleteMenuResult.Unauthorized -> {
        coVerify(exactly = 0) { menuRepository.delete(any()) }
      }
      else -> assert(false) { "Expected Unauthorized, but got $result" }
    }
  }
}
