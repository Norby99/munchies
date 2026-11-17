package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantKitchenService
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupCommand
import com.munchies.restaurant.application.usecase.restaurant.GetKitchenSetupResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateKitchenSetupResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toCommand
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetKitchenSetupResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateKitchenSetupRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateKitchenSetupResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Put

/**
 * The kitchen description of a restaurant, under `/restaurants/{restaurantId}/kitchen-setup`.
 *
 * Replaced as a whole by the restaurant's manager; meant to be read by the capacity agent of
 * scheduler-service. Like the capacity policy, it answers 404 until the manager describes it.
 */
@Controller("/restaurants")
class RestaurantKitchenController(
  private val kitchenService: RestaurantKitchenService,
) {

  @Get("/{restaurantId}/kitchen-setup")
  suspend fun getKitchenSetup(
    @PathVariable restaurantId: String,
  ): HttpResponse<GetKitchenSetupResponse> {
    val command = GetKitchenSetupCommand(restaurantId)
    return when (val result = kitchenService.getKitchenSetup(command)) {
      is GetKitchenSetupResult.Success -> HttpResponse.ok(result.toResponse())
      is GetKitchenSetupResult.InvalidRestaurant -> throw ValidationException(result.error)
      GetKitchenSetupResult.RestaurantNotFound -> throw NotFoundException("Restaurant not found")
      GetKitchenSetupResult.NotConfigured -> throw NotFoundException("Kitchen setup not configured")
    }
  }

  @Put("/{restaurantId}/kitchen-setup")
  suspend fun updateKitchenSetup(
    @PathVariable restaurantId: String,
    @Body request: UpdateKitchenSetupRequest,
  ): HttpResponse<UpdateKitchenSetupResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = kitchenService.updateKitchenSetup(command)) {
      is UpdateKitchenSetupResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateKitchenSetupResult.InvalidRestaurant -> throw ValidationException(result.error)
      is UpdateKitchenSetupResult.InvalidSetup -> throw ValidationException(result.error)
      UpdateKitchenSetupResult.NotFound -> throw NotFoundException("Restaurant not found")
      UpdateKitchenSetupResult.Unauthorized ->
        throw UnauthorizedException("Unauthorized to update the kitchen setup")
    }
  }
}
