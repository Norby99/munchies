package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.restaurant.GetCapacityPolicyCommand
import com.munchies.restaurant.application.usecase.restaurant.GetCapacityPolicyResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateCapacityPolicyCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateCapacityPolicyResult
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateCapacityPolicyRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantCapacityPolicyControllerTest {

  private lateinit var restaurantService: RestaurantService
  private lateinit var controller: RestaurantController

  @BeforeEach
  fun setUp() {
    restaurantService = mockk()
    controller = RestaurantController(restaurantService)
  }

  private val policy = CapacityPolicy(
    slotMinutes = 30,
    maxCapacityUnits = 20,
    opening = LocalTime.of(11, 0),
    closing = LocalTime.of(22, 0),
    prepLeadMinutes = 30,
    maxAdvanceDays = 7,
    maxTripsPerSlot = 2,
    maxTripKmBudget = 12.0,
    maxDetourKm = 0.5,
  )

  private fun request() = UpdateCapacityPolicyRequest(
    managerId = "manager-1",
    slotMinutes = 30,
    maxCapacityUnits = 20,
    opening = "11:00",
    closing = "22:00",
    prepLeadMinutes = 30,
    maxAdvanceDays = 7,
    maxTripsPerSlot = 2,
    maxTripKmBudget = 12.0,
    maxDetourKm = 0.5,
  )

  // --- GET ---

  @Test
  fun `get should return 200 with the policy wire form`() = runBlocking {
    coEvery { restaurantService.getCapacityPolicy(any()) } returns
      GetCapacityPolicyResult.Success(policy)

    val response = controller.getCapacityPolicy("restaurant-1")

    assertEquals(HttpStatus.OK, response.status)
    val dto = response.body().result
    assertEquals("11:00", dto.opening)
    assertEquals("22:00", dto.closing)
    assertEquals(20, dto.maxCapacityUnits)
    assertEquals(12.0, dto.maxTripKmBudget)
    coVerify {
      restaurantService.getCapacityPolicy(GetCapacityPolicyCommand("restaurant-1"))
    }
  }

  @Test
  fun `get should throw NotFoundException when no policy is configured`() = runBlocking {
    coEvery { restaurantService.getCapacityPolicy(any()) } returns
      GetCapacityPolicyResult.NotConfigured

    val exception = assertThrows<NotFoundException> {
      controller.getCapacityPolicy("restaurant-1")
    }

    assertEquals("Capacity policy not configured", exception.message)
  }

  @Test
  fun `get should throw NotFoundException for an unknown restaurant`() = runBlocking {
    coEvery { restaurantService.getCapacityPolicy(any()) } returns
      GetCapacityPolicyResult.RestaurantNotFound

    val exception = assertThrows<NotFoundException> {
      controller.getCapacityPolicy("missing")
    }

    assertEquals("Restaurant not found", exception.message)
  }

  // --- PUT ---

  @Test
  fun `put should return 200 with the saved policy`() = runBlocking {
    coEvery { restaurantService.updateCapacityPolicy(any()) } returns
      UpdateCapacityPolicyResult.Success(policy)

    val response = controller.updateCapacityPolicy("restaurant-1", request())

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("11:00", response.body().result.opening)
    coVerify {
      restaurantService.updateCapacityPolicy(
        UpdateCapacityPolicyCommand(
          managerId = "manager-1",
          restaurantId = "restaurant-1",
          slotMinutes = 30,
          maxCapacityUnits = 20,
          opening = "11:00",
          closing = "22:00",
          prepLeadMinutes = 30,
          maxAdvanceDays = 7,
          maxTripsPerSlot = 2,
          maxTripKmBudget = 12.0,
          maxDetourKm = 0.5,
        ),
      )
    }
  }

  @Test
  fun `put should throw ValidationException for a rejected policy`() = runBlocking {
    coEvery { restaurantService.updateCapacityPolicy(any()) } returns
      UpdateCapacityPolicyResult.InvalidPolicy("opening must be before closing")

    val exception = assertThrows<ValidationException> {
      controller.updateCapacityPolicy("restaurant-1", request())
    }

    assertEquals("opening must be before closing", exception.message)
  }

  @Test
  fun `put should throw UnauthorizedException for a non-owner manager`() = runBlocking {
    coEvery { restaurantService.updateCapacityPolicy(any()) } returns
      UpdateCapacityPolicyResult.Unauthorized

    assertThrows<UnauthorizedException> {
      controller.updateCapacityPolicy("restaurant-1", request())
    }
  }

  @Test
  fun `put should throw NotFoundException for a missing restaurant`() = runBlocking {
    coEvery { restaurantService.updateCapacityPolicy(any()) } returns
      UpdateCapacityPolicyResult.NotFound

    assertThrows<NotFoundException> {
      controller.updateCapacityPolicy("missing", request())
    }
  }
}
