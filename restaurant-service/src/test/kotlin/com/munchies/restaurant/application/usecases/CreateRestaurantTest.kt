package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.entity.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.Address
import com.munchies.restaurant.domain.valueobject.Email
import com.munchies.restaurant.domain.valueobject.Phone
import com.munchies.restaurant.domain.valueobject.RestaurantName
import com.munchies.restaurant.domain.valueobject.UserId
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("CreateRestaurant Tests")
class CreateRestaurantTest {

  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var createRestaurantUseCase: CreateRestaurantUseCase

  @BeforeEach
  fun setUp() {
    restaurantRepository = mockk()
    createRestaurantUseCase = CreateRestaurantUseCase(restaurantRepository)
  }

  @Test
  @DisplayName("Should create restaurant successfully when name does not exist")
  fun shouldCreateRestaurantSuccessfullyWhenNameDoesNotExist() = runBlocking {
    val command = validCommand()
    coEvery { restaurantRepository.findByManagerId(any()) } returns emptyList()
    coEvery { restaurantRepository.save(any()) } returns Unit

    when (val result = createRestaurantUseCase(command)) {
      is CreateRestaurantResult.Success -> {
        assertNotNull(result.restaurantId)
        coVerify(exactly = 1) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  @DisplayName(
    "Should fail with NameAlreadyExists when manager already has a restaurant with the same name",
  )
  fun shouldFailWhenManagerAlreadyHasRestaurantWithSameName() = runBlocking {
    val command = validCommand()
    val existingRestaurant = Restaurant.create(
      UserId.of(command.managerId),
      RestaurantDetails(
        name = RestaurantName.of(command.name),
        address = Address.of(command.address),
        phone = Phone.of(command.phone),
        email = Email.of(command.email),
      ),
    )
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existingRestaurant)

    when (val result = createRestaurantUseCase(command)) {
      is CreateRestaurantResult.NameAlreadyExists -> {
        coVerify(exactly = 0) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected NameAlreadyExists, but got $result" }
      }
    }
  }

  @Test
  @DisplayName("Should fail with invalid email")
  fun shouldFailWithInvalidEmail() = runBlocking {
    val command = CreateRestaurantCommand(
      managerId = "user-123",
      name = "Pizza Palace",
      address = "456 Oak Avenue",
      phone = "123456789",
      email = "invalid-email",
    )
    val result = createRestaurantUseCase(command)
    assertEquals(CreateRestaurantResult.ValidationError("Email format is invalid"), result)
  }

  @Test
  @DisplayName("Should fail with invalid phone")
  fun shouldFailWithInvalidPhone() = runBlocking {
    val command = CreateRestaurantCommand(
      managerId = "user-123",
      name = "Pizza Palace",
      address = "456 Oak Avenue",
      phone = "invalid!!!",
      email = "info@pizzapalace.com",
    )
    val result = createRestaurantUseCase(command)
    assertEquals(CreateRestaurantResult.ValidationError("Phone number format is invalid"), result)
  }

  @Test
  @DisplayName("Should fail with empty name")
  fun shouldFailWithEmptyName() = runBlocking {
    val command = CreateRestaurantCommand(
      managerId = "user-123",
      name = "   ",
      address = "456 Oak Avenue",
      phone = "123456789",
      email = "info@pizzapalace.com",
    )
    val result = createRestaurantUseCase(command)
    assertEquals(CreateRestaurantResult.ValidationError("Restaurant name cannot be blank"), result)
  }

  private fun validCommand(): CreateRestaurantCommand {
    return CreateRestaurantCommand(
      managerId = "user-123",
      name = "Pizza Palace",
      address = "456 Oak Avenue",
      phone = "123456789",
      email = "info@pizzapalace.com",
    )
  }
}
