package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecases.CreateRestaurantCommand
import com.munchies.restaurant.application.usecases.CreateRestaurantResult
import com.munchies.restaurant.application.usecases.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecases.DeleteRestaurantResult
import com.munchies.restaurant.application.usecases.GetRestaurantCommand
import com.munchies.restaurant.application.usecases.GetRestaurantResult
import com.munchies.restaurant.application.usecases.UpdateRestaurantCommand
import com.munchies.restaurant.application.usecases.UpdateRestaurantResult

interface RestaurantService {
  suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult
  suspend fun updateRestaurant(command: UpdateRestaurantCommand): UpdateRestaurantResult
  suspend fun deleteRestaurant(command: DeleteRestaurantCommand): DeleteRestaurantResult
  suspend fun getRestaurantDetails(command: GetRestaurantCommand): GetRestaurantResult
}
