package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsCommand
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ConflictException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantControllerTest {

  private lateinit var restaurantService: RestaurantService
  private lateinit var controller: RestaurantController

  @BeforeEach
  fun setUp() {
    restaurantService = mockk()
    controller = RestaurantController(restaurantService)
  }

  private fun sampleRestaurant(name: String = "Trattoria Roma"): Restaurant = Restaurant.create(
    managerId = UserId.of("manager-1"),
    name = RestaurantName.of(name),
    address = Address.of("Via Roma 1"),
    phone = Phone.of("+39 06 1234567"),
    email = Email.of("info@trattoria-roma.com"),
  )

  // --- Create ---

  @Test
  fun `should return 201 with the new restaurant id on success`() = runBlocking {
    coEvery { restaurantService.createRestaurant(any()) } returns
      CreateRestaurantResult.Success("restaurant-1")

    val response = controller.createRestaurant(
      CreateRestaurantRequest(
        managerId = "manager-1",
        name = "Trattoria Roma",
        address = "Via Roma 1",
        phone = "+39 06 1234567",
        email = "info@trattoria-roma.com",
      ),
    )

    assertEquals(HttpStatus.CREATED, response.status)
    assertEquals("restaurant-1", response.body().result)
    coVerify {
      restaurantService.createRestaurant(
        CreateRestaurantCommand(
          managerId = "manager-1",
          name = "Trattoria Roma",
          address = "Via Roma 1",
          phone = "+39 06 1234567",
          email = "info@trattoria-roma.com",
        ),
      )
    }
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid create`() = runBlocking {
    coEvery { restaurantService.createRestaurant(any()) } returns
      CreateRestaurantResult.InvalidRestaurant("blank name")

    val exception = assertThrows<ValidationException> {
      controller.createRestaurant(
        CreateRestaurantRequest(
          managerId = "manager-1",
          name = "",
          address = "Via Roma 1",
          phone = "+39 06 1234567",
          email = "info@trattoria-roma.com",
        ),
      )
    }

    assertEquals("blank name", exception.message)
  }

  @Test
  fun `should throw ConflictException when the restaurant name already exists on create`() =
    runBlocking {
      coEvery { restaurantService.createRestaurant(any()) } returns
        CreateRestaurantResult.NameAlreadyExists

      val exception = assertThrows<ConflictException> {
        controller.createRestaurant(
          CreateRestaurantRequest(
            managerId = "manager-1",
            name = "Trattoria Roma",
            address = "Via Roma 1",
            phone = "+39 06 1234567",
            email = "info@trattoria-roma.com",
          ),
        )
      }

      assertEquals("Name already exists", exception.message)
    }

  // --- Get ---

  @Test
  fun `should return 200 with the requested restaurant on success`() = runBlocking {
    val restaurant = sampleRestaurant()
    coEvery { restaurantService.getRestaurant(any()) } returns
      GetRestaurantResult.Success(restaurant)

    val response = controller.getRestaurant(restaurant.id.value)

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(restaurant.id.value, response.body().result.id)
    coVerify { restaurantService.getRestaurant(GetRestaurantCommand(restaurant.id.value)) }
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid get`() = runBlocking {
    coEvery { restaurantService.getRestaurant(any()) } returns
      GetRestaurantResult.InvalidRestaurant("bad id")

    val exception = assertThrows<ValidationException> {
      controller.getRestaurant("restaurant-1")
    }

    assertEquals("bad id", exception.message)
  }

  @Test
  fun `should throw NotFoundException when the restaurant does not exist on get`() = runBlocking {
    coEvery { restaurantService.getRestaurant(any()) } returns GetRestaurantResult.NotFound

    val exception = assertThrows<NotFoundException> {
      controller.getRestaurant("restaurant-1")
    }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `should return 200 with every manager restaurant on success`() = runBlocking {
    val first = sampleRestaurant("Trattoria Roma")
    val second = sampleRestaurant("Osteria Napoli")
    coEvery { restaurantService.getManagerRestaurants(any()) } returns
      GetManagerRestaurantsResult.Success(listOf(first, second))

    val response = controller.getManagerRestaurants("manager-1")

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(2, response.body().result.size)
    coVerify {
      restaurantService.getManagerRestaurants(GetManagerRestaurantsCommand("manager-1"))
    }
  }

  @Test
  fun `should throw ValidationException for a use case error when listing`() = runBlocking {
    coEvery { restaurantService.getManagerRestaurants(any()) } returns
      GetManagerRestaurantsResult.ValidationError("bad id")

    val exception = assertThrows<ValidationException> {
      controller.getManagerRestaurants("manager-1")
    }

    assertEquals("bad id", exception.message)
  }

  // --- Update ---

  @Test
  fun `should return 200 with the updated restaurant id on success`() = runBlocking {
    coEvery { restaurantService.updateRestaurant(any()) } returns
      UpdateRestaurantResult.Success("restaurant-1")

    val response = controller.updateRestaurant(
      "restaurant-1",
      UpdateRestaurantRequest(
        managerId = "manager-1",
        name = "New Name",
        address = "New Address",
        phone = "+39 06 7654321",
        email = "new@trattoria-roma.com",
      ),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("restaurant-1", response.body().result)
    coVerify {
      restaurantService.updateRestaurant(
        UpdateRestaurantCommand(
          managerId = "manager-1",
          restaurantId = "restaurant-1",
          name = "New Name",
          address = "New Address",
          phone = "+39 06 7654321",
          email = "new@trattoria-roma.com",
        ),
      )
    }
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid update`() = runBlocking {
    coEvery { restaurantService.updateRestaurant(any()) } returns
      UpdateRestaurantResult.InvalidRestaurant("blank name")

    val exception = assertThrows<ValidationException> {
      controller.updateRestaurant(
        "restaurant-1",
        UpdateRestaurantRequest(
          managerId = "manager-1",
          name = "",
          address = "New Address",
          phone = "+39 06 7654321",
          email = "new@trattoria-roma.com",
        ),
      )
    }

    assertEquals("blank name", exception.message)
  }

  @Test
  fun `should throw NotFoundException for a missing restaurant on update`() = runBlocking {
    coEvery { restaurantService.updateRestaurant(any()) } returns UpdateRestaurantResult.NotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateRestaurant(
        "restaurant-1",
        UpdateRestaurantRequest(
          managerId = "manager-1",
          name = "New Name",
          address = "New Address",
          phone = "+39 06 7654321",
          email = "new@trattoria-roma.com",
        ),
      )
    }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `should throw UnauthorizedException for an unauthorized manager on update`() = runBlocking {
    coEvery { restaurantService.updateRestaurant(any()) } returns
      UpdateRestaurantResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateRestaurant(
        "restaurant-1",
        UpdateRestaurantRequest(
          managerId = "manager-1",
          name = "New Name",
          address = "New Address",
          phone = "+39 06 7654321",
          email = "new@trattoria-roma.com",
        ),
      )
    }

    assertEquals("Unauthorized to update restaurant", exception.message)
  }

  @Test
  fun `should throw ConflictException when the restaurant name already exists on update`() =
    runBlocking {
      coEvery { restaurantService.updateRestaurant(any()) } returns
        UpdateRestaurantResult.NameAlreadyExists

      val exception = assertThrows<ConflictException> {
        controller.updateRestaurant(
          "restaurant-1",
          UpdateRestaurantRequest(
            managerId = "manager-1",
            name = "New Name",
            address = "New Address",
            phone = "+39 06 7654321",
            email = "new@trattoria-roma.com",
          ),
        )
      }

      assertEquals("Name already exists", exception.message)
    }

  // --- Delete ---

  @Test
  fun `should return 200 with the deleted restaurant id on success`() = runBlocking {
    coEvery { restaurantService.deleteRestaurant(any()) } returns
      DeleteRestaurantResult.Success("restaurant-1")

    val response = controller.deleteRestaurant(
      "restaurant-1",
      DeleteRestaurantRequest(managerId = "manager-1"),
    )

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("restaurant-1", response.body().result)
    coVerify {
      restaurantService.deleteRestaurant(DeleteRestaurantCommand("restaurant-1", "manager-1"))
    }
  }

  @Test
  fun `should throw ValidationException with the use case error on invalid delete`() = runBlocking {
    coEvery { restaurantService.deleteRestaurant(any()) } returns
      DeleteRestaurantResult.InvalidRestaurant("cannot delete")

    val exception = assertThrows<ValidationException> {
      controller.deleteRestaurant("restaurant-1", DeleteRestaurantRequest(managerId = "manager-1"))
    }

    assertEquals("cannot delete", exception.message)
  }

  @Test
  fun `should throw NotFoundException for a missing restaurant on delete`() = runBlocking {
    coEvery { restaurantService.deleteRestaurant(any()) } returns DeleteRestaurantResult.NotFound

    val exception = assertThrows<NotFoundException> {
      controller.deleteRestaurant("restaurant-1", DeleteRestaurantRequest(managerId = "manager-1"))
    }

    assertEquals("Restaurant not found", exception.message)
  }

  @Test
  fun `should throw UnauthorizedException for an unauthorized manager on delete`() = runBlocking {
    coEvery { restaurantService.deleteRestaurant(any()) } returns
      DeleteRestaurantResult.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.deleteRestaurant(
        "restaurant-1",
        DeleteRestaurantRequest(managerId = "manager-1"),
      )
    }

    assertEquals("Unauthorized to delete restaurant", exception.message)
  }
}
