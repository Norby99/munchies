package com.munchies.restaurant.bdd.application.menu

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuCommand
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.domain.aggregate.Category
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class MenuContext {
  lateinit var restaurantId: String
  lateinit var menuId: String
  lateinit var categoryId: String
  lateinit var itemId: String
  var lastResult: Any? = null
}

@Singleton
class MenuHelper @Inject constructor(private val service: MenuService) {
  fun createMenu(restaurantId: String, name: String, start: String, end: String): CreateMenuResult {
    val command = CreateMenuCommand(
      restaurantId = restaurantId,
      name = name,
      validity = ValidityInput.Period(start, end),
    )
    return runBlocking { service.createMenu(command) }
  }

  fun getMenu(restaurantId: String, menuId: String): GetMenuResult {
    val command = GetMenuCommand(restaurantId, menuId)
    return runBlocking { service.getMenu(command) }
  }

  fun getCategory(restaurantId: String, menuId: String, categoryId: String): Category {
    val command = GetMenuCommand(restaurantId, menuId)
    val result = runBlocking { service.getMenu(command) }
    check(result is GetMenuResult.Success) { "Retrieve menu failed" }
    return result.menu.categories.first { it.id.value == categoryId }
  }
}
