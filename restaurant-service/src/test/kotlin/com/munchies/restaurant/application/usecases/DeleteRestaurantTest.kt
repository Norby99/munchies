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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MicronautTest
@DisplayName("DeleteRestaurant Tests")
class DeleteRestaurantTest {

  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var deleteRestaurantUseCase: DeleteRestaurantUseCase

  @BeforeEach
  fun setUp() {
    restaurantRepository = mockk()
    deleteRestaurantUseCase = DeleteRestaurantUseCase(restaurantRepository)
  }

  @Test
  @DisplayName("Should delete restaurant successfully when authorized")
  fun shouldDeleteRestaurantSuccessfully() = runBlocking {
    val restaurantId = RestaurantId()
    val managerId = UserId.of("user-123")
    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Pizza Palace"),
        address = Address.of("123 Main St"),
        phone = Phone.of("123456789"),
        email = Email.of("info@pizzapalace.com"),
      ),
      createdAt = java.time.LocalDateTime.now(),
      updatedAt = java.time.LocalDateTime.now(),
    )

    val command = DeleteRestaurantCommand(
      restaurantId = restaurantId.value,
      managerId = "user-123",
    )

    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns restaurant
    coEvery { restaurantRepository.deleteById(restaurantId) } returns true

    val result = deleteRestaurantUseCase(command)

    assertEquals(
      DeleteRestaurantResult.Success(restaurantId.value),
      result,
    )
    coVerify(exactly = 1) { restaurantRepository.deleteById(restaurantId) }
  }

  @Test
  @DisplayName("Should return NotFound when restaurant does not exist")
  fun shouldReturnNotFoundWhenRestaurantDoesNotExist() = runBlocking {
    val restaurantId = RestaurantId()
    val command = DeleteRestaurantCommand(
      restaurantId = restaurantId.value,
      managerId = "user-123",
    )

    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns null

    val result = deleteRestaurantUseCase(command)

    assertEquals(DeleteRestaurantResult.NotFound, result)
    coVerify(exactly = 0) { restaurantRepository.deleteById(any()) }
  }

  @Test
  @DisplayName("Should return Unauthorized when user is not the restaurant manager")
  fun shouldReturnUnauthorizedWhenUserIsNotManager() = runBlocking {
    val restaurantId = RestaurantId()
    val managerId = UserId.of("user-456")
    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Pizza Palace"),
        address = Address.of("123 Main St"),
        phone = Phone.of("123456789"),
        email = Email.of("info@pizzapalace.com"),
      ),
      createdAt = java.time.LocalDateTime.now(),
      updatedAt = java.time.LocalDateTime.now(),
    )

    val command = DeleteRestaurantCommand(
      restaurantId = restaurantId.value,
      managerId = "user-123",
    )

    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns restaurant

    val result = deleteRestaurantUseCase(command)

    assertEquals(DeleteRestaurantResult.Unauthorized, result)
    coVerify(exactly = 0) { restaurantRepository.deleteById(any()) }
  }

  @Test
  @DisplayName("Should return ValidationError when restaurantId format is blank")
  fun shouldReturnValidationErrorWhenRestaurantIdIsInvalid() = runBlocking {
    val command = DeleteRestaurantCommand(
      restaurantId = "   ",
      managerId = "user-123",
    )

    val result = deleteRestaurantUseCase(command)

    assertEquals(
      DeleteRestaurantResult.ValidationError("RestaurantId cannot be blank"),
      result,
    )
    coVerify(exactly = 0) { restaurantRepository.findByIdSuspend(any()) }
  }

  @Test
  @DisplayName("Should return NotFound when deletion returns false")
  fun shouldReturnNotFoundWhenDeletionReturnsFalse() = runBlocking {
    val restaurantId = RestaurantId()
    val managerId = UserId.of("user-123")
    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Pizza Palace"),
        address = Address.of("123 Main St"),
        phone = Phone.of("123456789"),
        email = Email.of("info@pizzapalace.com"),
      ),
      createdAt = java.time.LocalDateTime.now(),
      updatedAt = java.time.LocalDateTime.now(),
    )

    val command = DeleteRestaurantCommand(
      restaurantId = restaurantId.value,
      managerId = "user-123",
    )

    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns restaurant
    coEvery { restaurantRepository.deleteById(restaurantId) } returns false

    val result = deleteRestaurantUseCase(command)

    assertEquals(DeleteRestaurantResult.NotFound, result)
  }
}
