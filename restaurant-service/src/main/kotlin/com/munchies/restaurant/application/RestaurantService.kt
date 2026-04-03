package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecases.CreateRestaurantCommand
import com.munchies.restaurant.application.usecases.CreateRestaurantResult
import com.munchies.restaurant.application.usecases.DeleteRestaurantCommand
import com.munchies.restaurant.application.usecases.GetRestaurantDetailsQuery
import com.munchies.restaurant.domain.entity.Restaurant

interface RestaurantService {
  suspend fun createRestaurant(command: CreateRestaurantCommand): CreateRestaurantResult
  suspend fun getRestaurantDetails(query: GetRestaurantDetailsQuery): Restaurant?
  suspend fun deleteRestaurant(command: DeleteRestaurantCommand): Boolean
}
