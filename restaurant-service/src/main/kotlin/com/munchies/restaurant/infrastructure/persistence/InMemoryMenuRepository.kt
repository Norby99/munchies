package com.munchies.restaurant.infrastructure.persistence

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["test"])
class InMemoryMenuRepository : InMemoryRepository<MenuId, Menu>()
