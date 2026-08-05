package com.munchies.restaurant.infrastructure.persistence

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["test"])
class InMemoryRestaurantRepository : RestaurantRepository,
  InMemoryRepository<RestaurantId, Restaurant>() {

  override fun findAllByManagerId(managerId: UserId): List<Restaurant> =
    findAllByPredicate { it.managerId == managerId }
}
