package com.munchies.restaurant.bdd

import com.munchies.restaurant.application.MenuApplicationService
import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.RestaurantApplicationService
import com.munchies.restaurant.application.RestaurantService
import com.munchies.restaurant.application.usecase.menu.CategoryUseCases
import com.munchies.restaurant.application.usecase.menu.MenuItemUseCases
import com.munchies.restaurant.application.usecase.menu.MenuUseCases
import com.munchies.restaurant.application.usecase.restaurant.RestaurantUseCases
import com.munchies.restaurant.infrastructure.persistence.InMemoryMenuRepository
import com.munchies.restaurant.infrastructure.persistence.InMemoryRestaurantRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class TestServicesFactory {

  @Singleton
  fun menuUseCases(repository: InMemoryMenuRepository): MenuUseCases {
    return MenuUseCases(repository)
  }

  @Singleton
  fun categoryUseCases(repository: InMemoryMenuRepository): CategoryUseCases {
    return CategoryUseCases(repository)
  }

  @Singleton
  fun menuItemUseCases(repository: InMemoryMenuRepository): MenuItemUseCases {
    return MenuItemUseCases(repository)
  }

  @Singleton
  fun menuService(
    menuUseCases: MenuUseCases,
    categoryUseCases: CategoryUseCases,
    menuItemUseCases: MenuItemUseCases,
  ): MenuService {
    return MenuApplicationService(
      menuUseCases = menuUseCases,
      categoryUseCases = categoryUseCases,
      menuItemUseCases = menuItemUseCases,
    )
  }

  @Singleton
  fun restaurantUseCases(repository: InMemoryRestaurantRepository): RestaurantUseCases {
    return RestaurantUseCases(repository)
  }

  @Singleton
  fun restaurantService(restaurantUseCases: RestaurantUseCases): RestaurantService {
    return RestaurantApplicationService(
      restaurantUseCases = restaurantUseCases,
    )
  }
}
