package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.CookStationSpec
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupResult
import com.munchies.restaurant.domain.valueobject.restaurant.KitchenSetup
import com.munchies.restaurant.infrastructure.adapter.dto.CookStationDto
import com.munchies.restaurant.infrastructure.adapter.dto.KitchenSetupDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetKitchenSetupResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateKitchenSetupRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateKitchenSetupResponse

fun KitchenSetup.toDto(): KitchenSetupDto = KitchenSetupDto(
  route = route.name,
  prepWorkers = prepWorkers,
  cookStations = cookStations.map { CookStationDto(name = it.name, places = it.places) }
    .toTypedArray(),
  prepMinutes = prepMinutes,
  loadingMinutes = loadingMinutes,
  cookMinutes = cookMinutes,
)

fun GetKitchenSetupResult.Success.toResponse(): GetKitchenSetupResponse =
  GetKitchenSetupResponse(setup.toDto())

fun UpdateKitchenSetupRequest.toCommand(restaurantId: String): UpdateKitchenSetupCommand =
  UpdateKitchenSetupCommand(
    managerId = managerId,
    restaurantId = restaurantId,
    route = route,
    prepWorkers = prepWorkers,
    cookStations = cookStations.map { CookStationSpec(name = it.name, places = it.places) },
    prepMinutes = prepMinutes,
    loadingMinutes = loadingMinutes,
    cookMinutes = cookMinutes,
  )

fun UpdateKitchenSetupResult.Success.toResponse(): UpdateKitchenSetupResponse =
  UpdateKitchenSetupResponse(setup.toDto())
