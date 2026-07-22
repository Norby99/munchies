package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
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
  fun `should delete restaurant successfully when authorized`() = runBlocking {
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

    coEvery { restaurantRepository.findById(restaurantId) } returns restaurant
    coEvery { restaurantRepository.delete(restaurant) } returns Unit

    val result = deleteRestaurantUseCase(command)

    assertEquals(
      DeleteRestaurantResult.Success(restaurantId.value),
      result,
    )
    coVerify(exactly = 1) { restaurantRepository.delete(restaurant) }
  }

  @Test
  fun `should return NotFound when restaurant does not exist`() = runBlocking {
    val restaurantId = RestaurantId()
    val command = DeleteRestaurantCommand(
      restaurantId = restaurantId.value,
      managerId = "user-123",
    )

    coEvery { restaurantRepository.findById(restaurantId) } returns null

    val result = deleteRestaurantUseCase(command)

    assertEquals(DeleteRestaurantResult.NotFound, result)
    coVerify(exactly = 0) { restaurantRepository.delete(any()) }
  }

  @Test
  fun `should return Unauthorized when user is not the restaurant manager`() = runBlocking {
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

    coEvery { restaurantRepository.findById(restaurantId) } returns restaurant

    val result = deleteRestaurantUseCase(command)

    assertEquals(DeleteRestaurantResult.Unauthorized, result)
    coVerify(exactly = 0) { restaurantRepository.delete(any()) }
  }

  @Test
  fun `should return InvalidRestaurant when restaurantId format is blank`() = runBlocking {
    val command = DeleteRestaurantCommand(
      restaurantId = "   ",
      managerId = "user-123",
    )

    val result = deleteRestaurantUseCase(command)

    assertEquals(
      DeleteRestaurantResult.InvalidRestaurant("RestaurantId cannot be blank"),
      result,
    )
    coVerify(exactly = 0) { restaurantRepository.findById(any()) }
  }

}
