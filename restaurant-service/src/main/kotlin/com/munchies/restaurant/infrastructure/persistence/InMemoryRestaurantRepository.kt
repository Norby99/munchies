package com.munchies.restaurant.infrastructure.persistence

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.valueobject.RestaurantId
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["test"])
class InMemoryRestaurantRepository : InMemoryRepository<RestaurantId, Restaurant>()
