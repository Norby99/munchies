package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantLocationResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantLocationCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantLocationResult
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.infrastructure.adapter.dto.GeoPointDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantLocationResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantLocationRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantLocationResponse

fun GeoPoint.toDto(): GeoPointDto = GeoPointDto(lat = lat, lng = lng)

fun GetRestaurantLocationResult.Success.toResponse(): GetRestaurantLocationResponse =
  GetRestaurantLocationResponse(location.toDto())

fun UpdateRestaurantLocationRequest.toCommand(
  restaurantId: String,
): UpdateRestaurantLocationCommand = UpdateRestaurantLocationCommand(
  managerId = managerId,
  restaurantId = restaurantId,
  lat = lat,
  lng = lng,
)

fun UpdateRestaurantLocationResult.Success.toResponse(): UpdateRestaurantLocationResponse =
  UpdateRestaurantLocationResponse(location.toDto())
