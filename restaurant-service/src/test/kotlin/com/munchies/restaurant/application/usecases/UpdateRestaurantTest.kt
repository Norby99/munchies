package com.munchies.restaurant.application.usecases

import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.entity.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.Address
import com.munchies.restaurant.domain.valueobject.Email
import com.munchies.restaurant.domain.valueobject.Phone
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.RestaurantName
import com.munchies.restaurant.domain.valueobject.UserId
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("UpdateRestaurant Tests")
class UpdateRestaurantTest {

  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var updateRestaurantUseCase: UpdateRestaurantUseCase

  @BeforeEach
  fun setUp() {
    restaurantRepository = mockk()
    updateRestaurantUseCase = UpdateRestaurantUseCase(restaurantRepository)
  }

  @Test
  @DisplayName("Should update restaurant successfully with all valid details")
  fun shouldUpdateRestaurantSuccessfully() = runBlocking {
    val existing: Restaurant = validRestaurant()

    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "New Name",
      address = "New Address",
      phone = "222222222",
      email = "new@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)
    coEvery { restaurantRepository.save(any()) } returns Unit

    when (val result = updateRestaurantUseCase(command)) {
      is UpdateRestaurantResult.Success -> {
        assertEquals(existing.id.value, result.restaurantId)
        coVerify(exactly = 1) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  @DisplayName("Should fail with NotFound when restaurant does not exist")
  fun shouldFailWhenRestaurantNotFound() = runBlocking {
    val command = validUpdateCommand()
    coEvery { restaurantRepository.findByIdSuspend(any()) } returns null

    when (val result = updateRestaurantUseCase(command)) {
      is UpdateRestaurantResult.NotFound -> {
        coVerify(exactly = 0) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected NotFound, but got $result" }
      }
    }
  }

  @Test
  @DisplayName("Should fail with Unauthorized when user is not the restaurant manager")
  fun shouldFailWhenUserIsNotManager() = runBlocking {
    val existing: Restaurant = validRestaurant()

    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "not-the-manager",
      name = "New Name",
      address = "New Address",
      phone = "222222222",
      email = "new@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing

    when (val result = updateRestaurantUseCase(command)) {
      is UpdateRestaurantResult.Unauthorized -> {
        coVerify(exactly = 0) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Unauthorized, but got $result" }
      }
    }
  }

  @Test
  @DisplayName(
    "Should fail with NameAlreadyExists when manager has another restaurant with the same name",
  )
  fun shouldFailWhenNameAlreadyExistsForManager() = runBlocking {
    val existing: Restaurant = validRestaurant()
    val anotherRestaurantWithNewName = Restaurant.create(
      existing.managerId,
      RestaurantDetails(
        name = RestaurantName.of("New Name"),
        address = Address.of("Another Address"),
        phone = Phone.of("333333333"),
        email = Email.of("another@example.com"),
      ),
    )
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "New Name",
      address = "New Address",
      phone = "222222222",
      email = "new@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery {
      restaurantRepository.findByManagerId(
        any(),
      )
    } returns listOf(anotherRestaurantWithNewName)

    when (val result = updateRestaurantUseCase(command)) {
      is UpdateRestaurantResult.NameAlreadyExists -> {
        coVerify(exactly = 0) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected NameAlreadyExists, but got $result" }
      }
    }
  }

  @Test
  @DisplayName("Should allow updating with same name when it is the current restaurant")
  fun shouldAllowUpdatingWithSameName() = runBlocking {
    val existing: Restaurant = validRestaurant()
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "Pizza Palace",
      address = "New Address",
      phone = "222222222",
      email = "new@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)
    coEvery { restaurantRepository.save(any()) } returns Unit

    when (val result = updateRestaurantUseCase(command)) {
      is UpdateRestaurantResult.Success -> {
        assertEquals(existing.id.value, result.restaurantId)
        coVerify(exactly = 1) { restaurantRepository.save(any()) }
      }
      else -> {
        assert(false) { "Expected Success, but got $result" }
      }
    }
  }

  @Test
  @DisplayName("Should fail with ValidationError when email format is invalid")
  fun shouldFailWithInvalidEmail() = runBlocking {
    val existing = validRestaurant()
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "Pizza Palace",
      address = "123 Main St",
      phone = "123456789",
      email = "invalid-email",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)

    val result = updateRestaurantUseCase(command)
    assertEquals(
      UpdateRestaurantResult.ValidationError("Email format is invalid"),
      result,
    )
  }

  @Test
  @DisplayName("Should fail with ValidationError when phone format is invalid")
  fun shouldFailWithInvalidPhone() = runBlocking {
    val existing: Restaurant = validRestaurant()
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "Pizza Palace",
      address = "123 Main St",
      phone = "invalid!!!",
      email = "info@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)

    val result = updateRestaurantUseCase(command)
    assertEquals(
      UpdateRestaurantResult.ValidationError("Phone number format is invalid"),
      result,
    )
  }

  @Test
  @DisplayName("Should fail with ValidationError when name is empty")
  fun shouldFailWithEmptyName() = runBlocking {
    val existing: Restaurant = validRestaurant()
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "   ",
      address = "123 Main St",
      phone = "123456789",
      email = "info@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)

    val result = updateRestaurantUseCase(command)
    assertEquals(
      UpdateRestaurantResult.ValidationError("Restaurant name cannot be blank"),
      result,
    )
  }

  @Test
  @DisplayName("Should fail with ValidationError when address is empty")
  fun shouldFailWithEmptyAddress() = runBlocking {
    val existing: Restaurant = validRestaurant()
    val command = UpdateRestaurantCommand(
      restaurantId = existing.id.value,
      managerId = "user-123",
      name = "Pizza Palace",
      address = "   ",
      phone = "123456789",
      email = "info@example.com",
    )

    coEvery { restaurantRepository.findByIdSuspend(any()) } returns existing
    coEvery { restaurantRepository.findByManagerId(any()) } returns listOf(existing)

    val result = updateRestaurantUseCase(command)
    assertEquals(
      UpdateRestaurantResult.ValidationError("Address cannot be blank"),
      result,
    )
  }

  private fun validRestaurant(): Restaurant {
    val restaurantId = RestaurantId()
    val managerId = UserId.of("user-123")
    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Pizza Palace"),
        address = Address.of("Old Address"),
        phone = Phone.of("111111111"),
        email = Email.of("old@example.com"),
      ),
      createdAt = LocalDateTime.now(),
      updatedAt = LocalDateTime.now(),
    )
    return restaurant
  }

  private fun validUpdateCommand(): UpdateRestaurantCommand {
    return UpdateRestaurantCommand(
      restaurantId = RestaurantId().value,
      managerId = "user-123",
      name = "Updated Pizza Palace",
      address = "789 New Street",
      phone = "987654321",
      email = "updated@pizzapalace.com",
    )
  }
}
