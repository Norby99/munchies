package com.munchies.restaurant.application.usecases.restaurant

import com.munchies.restaurant.domain.entity.Restaurant
import com.munchies.restaurant.domain.entity.RestaurantDetails
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteRestaurantTest {

  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var deleteRestaurantUseCase: DeleteRestaurantUseCase

  @BeforeEach
  fun setUp() {
    restaurantRepository = mockk()
    deleteRestaurantUseCase = DeleteRestaurantUseCase(restaurantRepository)
  }

  @Test
  fun `Should delete restaurant successfully when authorized`() = runBlocking {
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
  fun `Should return NotFound when restaurant does not exist`() = runBlocking {
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
  fun `Should return Unauthorized when user is not the restaurant manager`() = runBlocking {
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
  fun `Should return ValidationError when restaurantId format is blank`() = runBlocking {
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
  fun `Should return NotFound when deletion returns false`() = runBlocking {
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
