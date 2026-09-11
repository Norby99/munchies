package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RestaurantMapperTest {

  private fun sampleRestaurant(name: String = "Trattoria Roma"): Restaurant = Restaurant.create(
    managerId = UserId.of("manager-1"),
    name = RestaurantName.of(name),
    address = Address.of("Via Roma 1"),
    phone = Phone.of("+39 06 1234567"),
    email = Email.of("info@trattoria-roma.com"),
  )

  // --- Create ---

  @Test
  fun `should build a CreateRestaurantCommand from every request field`() {
    val request = CreateRestaurantRequest(
      managerId = "manager-1",
      name = "Trattoria Roma",
      address = "Via Roma 1",
      phone = "+39 06 1234567",
      email = "info@trattoria-roma.com",
    )

    val command = request.toCommand()

    assertEquals("manager-1", command.managerId)
    assertEquals("Trattoria Roma", command.name)
    assertEquals("Via Roma 1", command.address)
    assertEquals("+39 06 1234567", command.phone)
    assertEquals("info@trattoria-roma.com", command.email)
  }

  @Test
  fun `should wrap the new restaurant id in a 201 response`() {
    val result = CreateRestaurantResult.Success(restaurantId = "restaurant-1")

    val response = result.toResponse()

    assertEquals("restaurant-1", response.result)
    assertEquals(201, response.code)
  }

  // --- Get ---

  @Test
  fun `should wrap the restaurant dto in a 200 response`() {
    val restaurant = sampleRestaurant()

    val response = GetRestaurantResult.Success(restaurant).toResponse()

    assertEquals(restaurant.id.value, response.result.id)
    assertEquals(200, response.code)
  }

  @Test
  fun `should carry the manager id from the request into the command`() {
    val request = GetManagerRestaurantsRequest(managerId = "manager-1")

    val command = request.toCommand()

    assertEquals("manager-1", command.managerId)
  }

  @Test
  fun `should map every restaurant to its dto in a 200 response`() {
    val first = sampleRestaurant("Trattoria Roma")
    val second = sampleRestaurant("Osteria Napoli")

    val response = GetManagerRestaurantsResult.Success(listOf(first, second)).toResponse()

    assertEquals(2, response.result.size)
    assertEquals(first.id.value, response.result[0].id)
    assertEquals(second.id.value, response.result[1].id)
    assertEquals(200, response.code)
  }

  // --- Update ---

  @Test
  fun `should build an UpdateRestaurantCommand from the restaurant id and every request field`() {
    val request = UpdateRestaurantRequest(
      managerId = "manager-1",
      name = "New Name",
      address = "New Address",
      phone = "+39 06 7654321",
      email = "new@trattoria-roma.com",
    )

    val command = request.toCommand(restaurantId = "restaurant-1")

    assertEquals("manager-1", command.managerId)
    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("New Name", command.name)
    assertEquals("New Address", command.address)
    assertEquals("+39 06 7654321", command.phone)
    assertEquals("new@trattoria-roma.com", command.email)
  }

  @Test
  fun `should wrap the restaurant id in a 200 response`() {
    val result = UpdateRestaurantResult.Success(restaurantId = "restaurant-1")

    val response = result.toResponse()

    assertEquals("restaurant-1", response.result)
    assertEquals(200, response.code)
  }

  // --- Delete ---

  @Test
  fun `should build a DeleteRestaurantCommand from the restaurant id and the request manager id`() {
    val request = DeleteRestaurantRequest(managerId = "manager-1")

    val command = request.toCommand(restaurantId = "restaurant-1")

    assertEquals("restaurant-1", command.restaurantId)
    assertEquals("manager-1", command.managerId)
  }

  @Test
  fun `should wrap the deleted restaurant id in a 200 response`() {
    val result = DeleteRestaurantResult.Success(restaurantId = "restaurant-1")

    val response = result.toResponse()

    assertEquals("restaurant-1", response.result)
    assertEquals(200, response.code)
  }
}
