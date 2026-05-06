package com.munchies.restaurant.application.usecases.restaurant

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
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRestaurantTest {

  private lateinit var restaurantRepository: RestaurantRepository
  private lateinit var getRestaurantUseCase: GetRestaurantUseCase

  @BeforeEach
  fun setUp() {
    restaurantRepository = mockk()
    getRestaurantUseCase = GetRestaurantUseCase(restaurantRepository)
  }

  @Test
  fun `Should return Success with restaurant when restaurant exists`() = runBlocking {
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

    val command = GetRestaurantCommand(restaurantId.value)
    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns restaurant

    val result = getRestaurantUseCase(command)

    assertEquals(GetRestaurantResult.Success(restaurant), result)
  }

  @Test
  fun `Should return NotFound when restaurant does not exist`() = runBlocking {
    val restaurantId = RestaurantId()
    val command = GetRestaurantCommand(restaurantId.value)
    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns null

    val result = getRestaurantUseCase(command)

    assertEquals(GetRestaurantResult.NotFound, result)
  }

  @Test
  fun `Should return Success with all restaurant properties correctly`() = runBlocking {
    val restaurantId = RestaurantId()
    val managerId = UserId.of("manager-456")
    val createdAtTime = java.time.LocalDateTime.of(2026, 1, 15, 10, 30)
    val updatedAtTime = java.time.LocalDateTime.of(2026, 4, 3, 14, 20)

    val restaurant = Restaurant.fromDatabase(
      id = restaurantId,
      managerId = managerId,
      details = RestaurantDetails(
        name = RestaurantName.of("Burger House"),
        address = Address.of("456 Oak Avenue"),
        phone = Phone.of("987654321"),
        email = Email.of("contact@burgerhouse.com"),
      ),
      createdAt = createdAtTime,
      updatedAt = updatedAtTime,
    )

    val command = GetRestaurantCommand(restaurantId.value)
    coEvery { restaurantRepository.findByIdSuspend(restaurantId) } returns restaurant

    val result = getRestaurantUseCase(command)

    val successResult = result as? GetRestaurantResult.Success
    assertEquals(restaurant, successResult?.restaurant)
    assertEquals("Burger House", successResult?.restaurant?.details?.name?.value)
    assertEquals("456 Oak Avenue", successResult?.restaurant?.details?.address?.value)
    assertEquals("987654321", successResult?.restaurant?.details?.phone?.value)
    assertEquals("contact@burgerhouse.com", successResult?.restaurant?.details?.email?.value)
    assertEquals(managerId.value, successResult?.restaurant?.managerId?.value)
  }
}
