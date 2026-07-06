package com.munchies.restaurant.bdd.application

import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.GetRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateRestaurantResult
import com.munchies.restaurant.domain.aggregate.Restaurant
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class RestaurantContext {
  var currentUserId: String = "user-123"
  var managerId: String? = null
  var restaurantName: String? = null
  var restaurantAddress: String? = null
  var restaurantPhone: String? = null
  var restaurantEmail: String? = null
  var createdRestaurantId: String? = null
  var lastResult: Any? = null
  var retrievedRestaurant: Restaurant? = null
}

@Singleton
class RestaurantHelper @Inject constructor(
  private val service: RestaurantService,
) {
  fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult {
    return runBlocking { service.createRestaurant(command) }
  }

  fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return runBlocking { service.updateRestaurant(command) }
  }

  fun getRestaurantDetails(restaurantId: String): GetRestaurantResult {
    val command = GetRestaurantCommand(restaurantId)
    return runBlocking { service.getRestaurantDetails(command) }
  }

  fun deleteRestaurant(restaurantId: String, managerId: String): DeleteRestaurantResult {
    val command = DeleteRestaurantCommand(restaurantId, managerId)
    return runBlocking { service.deleteRestaurant(command) }
  }
}
