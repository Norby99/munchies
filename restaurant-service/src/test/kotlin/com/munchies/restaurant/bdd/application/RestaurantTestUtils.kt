package com.munchies.restaurant.bdd.application

import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.CreateRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecase.restaurant.DeleteRestaurantResult
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsCommand
import com.munchies.restaurant.application.usecase.restaurant.GetManagerRestaurantsResult
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
  var managerId: String? = null
  var restaurantId: String? = null
  var lastResult: Any? = null
}

@Singleton
class RestaurantHelper @Inject constructor(
  private val service: RestaurantService,
) {
  fun createRestaurant(
    managerId: String,
    name: String,
    address: String,
    phone: String,
    email: String,
  ): CreateRestaurantResult {
    val command = CreateRestaurantCommand(managerId, name, address, phone, email)
    return runBlocking { service.createRestaurant(command) }
  }

  fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult {
    return runBlocking { service.updateRestaurant(command) }
  }

  fun getRestaurantDetails(restaurantId: String): GetRestaurantResult {
    val command = GetRestaurantCommand(restaurantId)
    return runBlocking { service.getRestaurant(command) }
  }

  fun getManagerRestaurants(managerId: String): List<Restaurant> {
    val command = GetManagerRestaurantsCommand(managerId)
    val result = runBlocking { service.getManagerRestaurants(command) }
    require(result is GetManagerRestaurantsResult.Success) {
      "Failed to retrieve manager's restaurants"
    }
    return result.restaurants
  }

  fun deleteRestaurant(restaurantId: String, managerId: String): DeleteRestaurantResult {
    val command = DeleteRestaurantCommand(restaurantId, managerId)
    return runBlocking { service.deleteRestaurant(command) }
  }
}
