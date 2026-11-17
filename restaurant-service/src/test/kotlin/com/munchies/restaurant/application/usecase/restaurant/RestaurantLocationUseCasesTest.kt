package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RestaurantLocationUseCasesTest {

  private lateinit var repository: RestaurantRepository
  private lateinit var getUseCase: GetRestaurantLocationUseCase
  private lateinit var updateUseCase: UpdateRestaurantLocationUseCase
  private lateinit var manager: UserId
  private lateinit var restaurant: Restaurant

  @BeforeEach
  fun setUp() {
    repository = mockk(relaxed = true)
    getUseCase = GetRestaurantLocationUseCase(repository)
    updateUseCase = UpdateRestaurantLocationUseCase(repository)
    manager = UserId.of("manager-1")
    restaurant = Restaurant.create(
      managerId = manager,
      name = RestaurantName.of("Trattoria"),
      address = Address.of("Via Roma 1"),
      phone = Phone.of("+39 0541 000000"),
      email = Email.of("info@trattoria.com"),
    )
    every { repository.findById(restaurant.id) } returns restaurant
  }

  @Test
  fun `get reports not configured until a location is set`() {
    val result = kotlinx.coroutines.runBlocking {
      getUseCase(GetRestaurantLocationCommand(restaurant.id.value))
    }
    assertTrue(result is GetRestaurantLocationResult.NotConfigured)
  }

  @Test
  fun `owner can set the location`() {
    val result = kotlinx.coroutines.runBlocking {
      updateUseCase(
        UpdateRestaurantLocationCommand(manager.value, restaurant.id.value, 44.0, 12.6522),
      )
    }
    assertTrue(result is UpdateRestaurantLocationResult.Success)
    assertEquals(GeoPoint(44.0, 12.6522), restaurant.location)
  }

  @Test
  fun `another manager cannot set the location`() {
    val result = kotlinx.coroutines.runBlocking {
      updateUseCase(
        UpdateRestaurantLocationCommand("someone-else", restaurant.id.value, 44.0, 12.6522),
      )
    }
    assertTrue(result is UpdateRestaurantLocationResult.Unauthorized)
  }

  @Test
  fun `out-of-range coordinates are rejected`() {
    val result = kotlinx.coroutines.runBlocking {
      updateUseCase(
        UpdateRestaurantLocationCommand(manager.value, restaurant.id.value, 200.0, 12.0),
      )
    }
    assertTrue(result is UpdateRestaurantLocationResult.InvalidLocation)
  }
}
