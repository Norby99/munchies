package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsCommand
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse

// --- Create ---

fun CreateRestaurantRequest.toCommand(): CreateRestaurantCommand =
  CreateRestaurantCommand(managerId, name, address, phone, email)

fun CreateRestaurantResult.Success.toResponse(): CreateRestaurantResponse =
  CreateRestaurantResponse(restaurantId)

// --- Get ---

fun GetRestaurantResult.Success.toResponse(): GetRestaurantResponse =
  GetRestaurantResponse(restaurant.toDto())

// --- Get Manager Restaurants ---

fun GetManagerRestaurantsRequest.toCommand(): GetManagerRestaurantsCommand =
  GetManagerRestaurantsCommand(managerId)

fun GetManagerRestaurantsResult.Success.toResponse(): GetManagerRestaurantsResponse =
  GetManagerRestaurantsResponse(restaurants.map { it.toDto() }.toTypedArray())

// --- Update ---

fun UpdateRestaurantRequest.toCommand(restaurantId: String): UpdateRestaurantCommand =
  UpdateRestaurantCommand(managerId, restaurantId, name, address, phone, email)

fun UpdateRestaurantResult.Success.toResponse(): UpdateRestaurantResponse =
  UpdateRestaurantResponse(restaurantId)

// --- Delete ---

fun DeleteRestaurantRequest.toCommand(restaurantId: String): DeleteRestaurantCommand =
  DeleteRestaurantCommand(managerId, restaurantId)

fun DeleteRestaurantResult.Success.toResponse(): DeleteRestaurantResponse =
  DeleteRestaurantResponse(restaurantId)
