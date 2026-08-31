package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateMenuTest {

  private lateinit var menuRepository: MenuRepository
  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var createMenuUseCase: CreateMenuUseCase

  @BeforeEach
  fun setUp() {
    menuRepository = mockk()
    restaurantRepository = mockk()
    createMenuUseCase = CreateMenuUseCase(menuRepository, restaurantRepository)
  }

  private fun aRestaurant(): Restaurant = Restaurant.create(
    managerId = UserId(),
    name = RestaurantName.of("Trattoria da Piero"),
    address = Address.of("Via Roma 1, Milano"),
    phone = Phone.of("+39 333 1234567"),
    email = Email.of("piero@example.com"),
  )

  @Test
  fun `should add menu successfully with valid data`() = runBlocking {
    val restaurant = aRestaurant()
    val command = CreateMenuCommand(
      restaurantId = restaurant.id.value,
      managerId = restaurant.managerId.value,
      name = "Winter Menu",
      validity = ValidityInput.Period("2026-12-01", "2027-02-28"),
    )

    coEvery { restaurantRepository.findById(restaurant.id) } returns restaurant
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
    val restaurant = aRestaurant()
    val command = CreateMenuCommand(
      restaurantId = restaurant.id.value,
      managerId = restaurant.managerId.value,
      name = "Main Menu",
      validity = ValidityInput.Always,
    )

    coEvery { restaurantRepository.findById(restaurant.id) } returns restaurant
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
    val restaurant = aRestaurant()
    val command = CreateMenuCommand(
      restaurantId = restaurant.id.value,
      managerId = restaurant.managerId.value,
      name = "   ",
      validity = ValidityInput.Always,
    )

    coEvery { restaurantRepository.findById(restaurant.id) } returns restaurant

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.InvalidMenu -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected InvalidMenu, but got $result" }
    }
  }

  @Test
  fun `should fail when restaurant does not exist`() = runBlocking {
    val command = CreateMenuCommand(
      restaurantId = RestaurantId().value,
      managerId = UserId().value,
      name = "Winter Menu",
      validity = ValidityInput.Always,
    )

    coEvery { restaurantRepository.findById(any()) } returns null

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.RestaurantNotFound -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected RestaurantNotFound, but got $result" }
    }
  }

  @Test
  fun `should fail when caller is not the owning manager`() = runBlocking {
    val restaurant = aRestaurant()
    val command = CreateMenuCommand(
      restaurantId = restaurant.id.value,
      managerId = UserId().value,
      name = "Winter Menu",
      validity = ValidityInput.Always,
    )

    coEvery { restaurantRepository.findById(restaurant.id) } returns restaurant

    when (val result = createMenuUseCase(command)) {
      is CreateMenuResult.Unauthorized -> {
        coVerify(exactly = 0) { menuRepository.save(any()) }
      }
      else -> assert(false) { "Expected Unauthorized, but got $result" }
    }
  }
}
