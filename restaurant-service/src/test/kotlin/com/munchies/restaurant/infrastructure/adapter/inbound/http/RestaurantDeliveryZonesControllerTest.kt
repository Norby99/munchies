package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantDeliveryZonesService
import com.munchies.restaurant.application.usecase.restaurant.DeliveryZoneSpec
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.ZoneVertexSpec
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.infrastructure.adapter.dto.DeliveryZoneDto
import com.munchies.restaurant.infrastructure.adapter.dto.GeoPointDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateDeliveryZonesRequest
import io.micronaut.http.HttpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantDeliveryZonesControllerTest {

  private lateinit var zonesService: RestaurantDeliveryZonesService
  private lateinit var controller: RestaurantDeliveryZonesController

  private val zone = DeliveryZone(
    id = "z1",
    name = "Centro",
    boundary = listOf(GeoPoint(44.0, 12.6), GeoPoint(44.0, 12.7), GeoPoint(44.05, 12.65)),
  )

  @BeforeEach
  fun setUp() {
    zonesService = mockk()
    controller = RestaurantDeliveryZonesController(zonesService)
  }

  @Test
  fun `get should return 200 with the zones wire form`() = runBlocking {
    coEvery { zonesService.getDeliveryZones(any()) } returns
      GetDeliveryZonesResult.Success(listOf(zone))

    val response = controller.getDeliveryZones("restaurant-1")

    assertEquals(HttpStatus.OK, response.status)
    val dto = response.body().result.single()
    assertEquals("z1", dto.id)
    assertEquals("Centro", dto.name)
    assertEquals(3, dto.boundary.size)
    assertEquals(12.7, dto.boundary[1].lng)
    coVerify { zonesService.getDeliveryZones(GetDeliveryZonesCommand("restaurant-1")) }
  }

  @Test
  fun `get should throw NotFoundException for an unknown restaurant`() = runBlocking {
    coEvery { zonesService.getDeliveryZones(any()) } returns
      GetDeliveryZonesResult.RestaurantNotFound

    assertThrows<NotFoundException> { controller.getDeliveryZones("restaurant-1") }
    Unit
  }

  @Test
  fun `put should map the request to a command and return the saved zones`() = runBlocking {
    coEvery { zonesService.updateDeliveryZones(any()) } returns
      UpdateDeliveryZonesResult.Success(listOf(zone))
    val request = UpdateDeliveryZonesRequest(
      managerId = "manager-1",
      zones = arrayOf(
        DeliveryZoneDto(
          id = "",
          name = "Centro",
          boundary = arrayOf(
            GeoPointDto(44.0, 12.6),
            GeoPointDto(44.0, 12.7),
            GeoPointDto(44.05, 12.65),
          ),
        ),
      ),
    )

    val response = controller.updateDeliveryZones("restaurant-1", request)

    assertEquals(HttpStatus.OK, response.status)
    assertEquals("z1", response.body().result.single().id)
    coVerify {
      zonesService.updateDeliveryZones(
        UpdateDeliveryZonesCommand(
          managerId = "manager-1",
          restaurantId = "restaurant-1",
          zones = listOf(
            DeliveryZoneSpec(
              id = "",
              name = "Centro",
              boundary = listOf(
                ZoneVertexSpec(44.0, 12.6),
                ZoneVertexSpec(44.0, 12.7),
                ZoneVertexSpec(44.05, 12.65),
              ),
            ),
          ),
        ),
      )
    }
  }

  @Test
  fun `put should throw ValidationException for invalid zones`() = runBlocking {
    coEvery { zonesService.updateDeliveryZones(any()) } returns
      UpdateDeliveryZonesResult.InvalidZones("duplicated zone names: [centro]")

    val exception = assertThrows<ValidationException> {
      controller.updateDeliveryZones("restaurant-1", UpdateDeliveryZonesRequest("manager-1"))
    }
    assertEquals("duplicated zone names: [centro]", exception.message)
  }

  @Test
  fun `put should throw UnauthorizedException for another manager`() = runBlocking {
    coEvery { zonesService.updateDeliveryZones(any()) } returns
      UpdateDeliveryZonesResult.Unauthorized

    assertThrows<UnauthorizedException> {
      controller.updateDeliveryZones("restaurant-1", UpdateDeliveryZonesRequest("manager-2"))
    }
    Unit
  }
}
