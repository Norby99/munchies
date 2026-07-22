package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ConflictException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.*
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.toResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put

@Controller("/restaurants")
class RestaurantController(private val restaurantService: RestaurantService) {

  @Post
  suspend fun createRestaurant(
    @Body request: CreateRestaurantRequest,
  ): HttpResponse<CreateRestaurantResponse> {
    val command = request.toCommand()
    return when (val result = restaurantService.createRestaurant(command)) {
      is CreateRestaurantResult.Success -> HttpResponse.created(result.toResponse())
      is CreateRestaurantResult.InvalidRestaurant -> throw ValidationException(result.error)
      CreateRestaurantResult.NameAlreadyExists -> throw ConflictException("Name already exists")
    }
  }

  @Get("/{restaurantId}")
  suspend fun getRestaurant(
    @PathVariable restaurantId: String,
  ): HttpResponse<GetRestaurantResponse> {
    val command = GetRestaurantCommand(restaurantId)
    return when (val result = restaurantService.getRestaurant(command)) {
      is GetRestaurantResult.Success -> HttpResponse.ok(result.toResponse())
      is GetRestaurantResult.InvalidRestaurant -> throw ValidationException(result.error)
      GetRestaurantResult.NotFound -> throw NotFoundException("Restaurant not found")
    }
  }

  @Get
  suspend fun getManagerRestaurants(
    @Body request: GetManagerRestaurantsRequest,
  ): HttpResponse<GetManagerRestaurantsResponse> {
    val command = request.toCommand()
    return when (val result = restaurantService.getManagerRestaurants(command)) {
      is GetManagerRestaurantsResult.Success -> HttpResponse.ok(result.toResponse())
      is GetManagerRestaurantsResult.ValidationError -> throw ValidationException(result.error)
    }
  }

  @Put("/{restaurantId}")
  suspend fun updateRestaurant(
    @PathVariable restaurantId: String,
    @Body request: UpdateRestaurantRequest,
  ): HttpResponse<UpdateRestaurantResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = restaurantService.updateRestaurant(command)) {
      is UpdateRestaurantResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateRestaurantResult.InvalidRestaurant -> throw ValidationException(result.error)
      is UpdateRestaurantResult.NotFound -> throw NotFoundException("Restaurant not found")
      is UpdateRestaurantResult.Unauthorized ->
        throw UnauthorizedException("Unauthorized to update restaurant")
      is UpdateRestaurantResult.NameAlreadyExists -> throw ConflictException("Name already exists")
    }
  }

  @Delete("/{restaurantId}")
  suspend fun deleteRestaurant(
    @PathVariable restaurantId: String,
    @Body request: DeleteRestaurantRequest,
  ): HttpResponse<DeleteRestaurantResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = restaurantService.deleteRestaurant(command)) {
      is DeleteRestaurantResult.Success -> HttpResponse.ok(result.toResponse())
      is DeleteRestaurantResult.InvalidRestaurant -> throw ValidationException(result.error)
      DeleteRestaurantResult.NotFound -> throw NotFoundException("Restaurant not found")
      DeleteRestaurantResult.Unauthorized -> throw UnauthorizedException(
        "Unauthorized to delete restaurant",
      )
    }
  }
}
