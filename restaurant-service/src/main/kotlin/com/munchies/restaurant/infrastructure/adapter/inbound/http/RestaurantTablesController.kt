package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantTablesService
import com.munchies.restaurant.application.usecase.restaurant.GetTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.GetTablesResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toCommand
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetTablesResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Put

/**
 * The tables of a restaurant's dining room, under `/restaurants/{restaurantId}/tables`.
 *
 * They are read by table-reservation-service, which books reservations against them, and
 * replaced as a whole by the restaurant's manager.
 */
@Controller("/restaurants")
class RestaurantTablesController(private val tablesService: RestaurantTablesService) {

  @Get("/{restaurantId}/tables")
  suspend fun getTables(@PathVariable restaurantId: String): HttpResponse<GetTablesResponse> {
    val command = GetTablesCommand(restaurantId)
    return when (val result = tablesService.getTables(command)) {
      is GetTablesResult.Success -> HttpResponse.ok(result.toResponse())
      is GetTablesResult.InvalidRestaurant -> throw ValidationException(result.error)
      GetTablesResult.RestaurantNotFound -> throw NotFoundException("Restaurant not found")
    }
  }

  @Put("/{restaurantId}/tables")
  suspend fun updateTables(
    @PathVariable restaurantId: String,
    @Body request: UpdateTablesRequest,
  ): HttpResponse<UpdateTablesResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = tablesService.updateTables(command)) {
      is UpdateTablesResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateTablesResult.InvalidRestaurant -> throw ValidationException(result.error)
      is UpdateTablesResult.InvalidTables -> throw ValidationException(result.error)
      UpdateTablesResult.NotFound -> throw NotFoundException("Restaurant not found")
      UpdateTablesResult.Unauthorized ->
        throw UnauthorizedException("Unauthorized to update tables")
    }
  }
}
