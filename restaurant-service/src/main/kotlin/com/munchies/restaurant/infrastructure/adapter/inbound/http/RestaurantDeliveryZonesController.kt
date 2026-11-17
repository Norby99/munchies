package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantDeliveryZonesService
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetDeliveryZonesResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateDeliveryZonesResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toCommand
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetDeliveryZonesResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateDeliveryZonesRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateDeliveryZonesResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Put

/**
 * The delivery zones of a restaurant, under `/restaurants/{restaurantId}/delivery-zones`.
 *
 * They are read by scheduler-service, which batches on one driver trip only deliveries that share
 * a zone, and replaced as a whole by the restaurant's manager.
 */
@Controller("/restaurants")
class RestaurantDeliveryZonesController(
  private val zonesService: RestaurantDeliveryZonesService,
) {

  @Get("/{restaurantId}/delivery-zones")
  suspend fun getDeliveryZones(
    @PathVariable restaurantId: String,
  ): HttpResponse<GetDeliveryZonesResponse> {
    val command = GetDeliveryZonesCommand(restaurantId)
    return when (val result = zonesService.getDeliveryZones(command)) {
      is GetDeliveryZonesResult.Success -> HttpResponse.ok(result.toResponse())
      is GetDeliveryZonesResult.InvalidRestaurant -> throw ValidationException(result.error)
      GetDeliveryZonesResult.RestaurantNotFound -> throw NotFoundException("Restaurant not found")
    }
  }

  @Put("/{restaurantId}/delivery-zones")
  suspend fun updateDeliveryZones(
    @PathVariable restaurantId: String,
    @Body request: UpdateDeliveryZonesRequest,
  ): HttpResponse<UpdateDeliveryZonesResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = zonesService.updateDeliveryZones(command)) {
      is UpdateDeliveryZonesResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateDeliveryZonesResult.InvalidRestaurant -> throw ValidationException(result.error)
      is UpdateDeliveryZonesResult.InvalidZones -> throw ValidationException(result.error)
      UpdateDeliveryZonesResult.NotFound -> throw NotFoundException("Restaurant not found")
      UpdateDeliveryZonesResult.Unauthorized ->
        throw UnauthorizedException("Unauthorized to update delivery zones")
    }
  }
}
