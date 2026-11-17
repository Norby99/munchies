package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantKitchenService
import com.munchies.restaurant.application.usecase.restaurant.CookStationSpec
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupResult
import com.munchies.restaurant.domain.valueobject.restaurant.CookStation
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenRoute
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenSetup
import com.munchies.restaurant.infrastructure.adapter.dto.CookStationDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateKitchenSetupRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantKitchenControllerTest {

  private lateinit var kitchenService: RestaurantKitchenService
  private lateinit var controller: RestaurantKitchenController

  private val setup = KitchenSetup(
    route = KitchenRoute.PREP_THEN_COOK,
    prepWorkers = 2,
    cookStations = listOf(CookStation("Oven", 7), CookStation("Grill", 2)),
    prepMinutes = 3.0,
    loadingMinutes = 0.5,
    cookMinutes = 5.0,
  )

  private val request = UpdateKitchenSetupRequest(
    managerId = "manager-1",
    route = "PREP_THEN_COOK",
    prepWorkers = 2,
    cookStations = arrayOf(CookStationDto("Oven", 7), CookStationDto("Grill", 2)),
    prepMinutes = 3.0,
    loadingMinutes = 0.5,
    cookMinutes = 5.0,
  )

  @BeforeEach
  fun setUp() {
    kitchenService = mockk()
    controller = RestaurantKitchenController(kitchenService)
  }

  @Test
  fun `get should return 200 with the kitchen wire form`() = runBlocking {
    coEvery { kitchenService.getKitchenSetup(any()) } returns GetKitchenSetupResult.Success(setup)

    val response = controller.getKitchenSetup("restaurant-1")

    assertEquals(HttpStatus.OK, response.status)
    val dto = response.body().result
    assertEquals("PREP_THEN_COOK", dto.route)
    assertEquals(2, dto.prepWorkers)
    assertEquals(listOf("Oven" to 7, "Grill" to 2), dto.cookStations.map { it.name to it.places })
    assertEquals(0.5, dto.loadingMinutes)
    assertEquals(5.0, dto.cookMinutes)
    coVerify { kitchenService.getKitchenSetup(GetKitchenSetupCommand("restaurant-1")) }
  }

  @Test
  fun `get should throw NotFoundException when the kitchen is not configured`() = runBlocking {
    coEvery { kitchenService.getKitchenSetup(any()) } returns GetKitchenSetupResult.NotConfigured

    val error = assertThrows<NotFoundException> { controller.getKitchenSetup("restaurant-1") }
    assertEquals("Kitchen setup not configured", error.message)
  }

  @Test
  fun `put should map the request to a command and return the saved kitchen`() = runBlocking {
    coEvery { kitchenService.updateKitchenSetup(any()) } returns
      UpdateKitchenSetupResult.Success(setup)

    val response = controller.updateKitchenSetup("restaurant-1", request)

    assertEquals(HttpStatus.OK, response.status)
    assertEquals(3.0, response.body().result.prepMinutes)
    coVerify {
      kitchenService.updateKitchenSetup(
        UpdateKitchenSetupCommand(
          managerId = "manager-1",
          restaurantId = "restaurant-1",
          route = "PREP_THEN_COOK",
          prepWorkers = 2,
          cookStations = listOf(CookStationSpec("Oven", 7), CookStationSpec("Grill", 2)),
          prepMinutes = 3.0,
          loadingMinutes = 0.5,
          cookMinutes = 5.0,
        ),
      )
    }
  }

  @Test
  fun `put should map the failures to HTTP errors`() = runBlocking {
    coEvery { kitchenService.updateKitchenSetup(any()) } returns
      UpdateKitchenSetupResult.InvalidSetup("bad")
    assertThrows<ValidationException> { controller.updateKitchenSetup("restaurant-1", request) }

    coEvery { kitchenService.updateKitchenSetup(any()) } returns UpdateKitchenSetupResult.NotFound
    assertThrows<NotFoundException> { controller.updateKitchenSetup("restaurant-1", request) }

    coEvery { kitchenService.updateKitchenSetup(any()) } returns
      UpdateKitchenSetupResult.Unauthorized
    assertThrows<UnauthorizedException> { controller.updateKitchenSetup("restaurant-1", request) }
    Unit
  }
}
