package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.DeliveryZoneSpec
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.ZoneVertexSpec
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.infrastructure.adapter.dto.DeliveryZoneDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetDeliveryZonesResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateDeliveryZonesRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateDeliveryZonesResponse

fun DeliveryZone.toDto(): DeliveryZoneDto = DeliveryZoneDto(
  id = id,
  name = name,
  boundary = boundary.map { it.toDto() }.toTypedArray(),
)

fun GetDeliveryZonesResult.Success.toResponse(): GetDeliveryZonesResponse =
  GetDeliveryZonesResponse(zones.map { it.toDto() }.toTypedArray())

fun UpdateDeliveryZonesRequest.toCommand(restaurantId: String): UpdateDeliveryZonesCommand =
  UpdateDeliveryZonesCommand(
    managerId = managerId,
    restaurantId = restaurantId,
    zones = zones.map { zone ->
      DeliveryZoneSpec(
        id = zone.id,
        name = zone.name,
        boundary = zone.boundary.map { ZoneVertexSpec(lat = it.lat, lng = it.lng) },
      )
    },
  )

fun UpdateDeliveryZonesResult.Success.toResponse(): UpdateDeliveryZonesResponse =
  UpdateDeliveryZonesResponse(zones.map { it.toDto() }.toTypedArray())
