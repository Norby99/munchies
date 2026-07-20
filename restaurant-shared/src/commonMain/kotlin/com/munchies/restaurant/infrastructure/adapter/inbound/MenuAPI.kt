package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import kotlin.js.JsExport

@JsExport
object MenuAPI {

  // ---- Menu ----

  interface CreateMenuAPI<Response> {
    fun createMenu(restaurantId: String, request: CreateMenuRequest): Response
  }

  interface GetMenuAPI<Response> {
    fun getMenu(restaurantId: String, menuId: String): Response
  }

  interface GetRestaurantMenusAPI<Response> {
    fun getRestaurantMenus(restaurantId: String): Response
  }

  interface UpdateMenuAPI<Response> {
    fun updateMenu(restaurantId: String, menuId: String, request: UpdateMenuRequest): Response
  }

  interface DeleteMenuAPI<Response> {
    fun deleteMenu(restaurantId: String, menuId: String): Response
  }

  // ---- Category ----

  interface CreateCategoryAPI<Response> {
    fun createCategory(
      restaurantId: String,
      menuId: String,
      request: CreateCategoryRequest,
    ): Response
  }

  interface UpdateCategoryAPI<Response> {
    fun updateCategory(
      restaurantId: String,
      menuId: String,
      categoryId: String,
      request: UpdateCategoryRequest,
    ): Response
  }

  interface DeleteCategoryAPI<Response> {
    fun deleteCategory(restaurantId: String, menuId: String, categoryId: String): Response
  }

  // ---- Menu Item ----

  interface CreateMenuItemAPI<Response> {
    fun createMenuItem(
      restaurantId: String,
      menuId: String,
      categoryId: String,
      request: CreateMenuItemRequest,
    ): Response
  }

  interface UpdateMenuItemAPI<Response> {
    fun updateMenuItem(
      restaurantId: String,
      menuId: String,
      categoryId: String,
      itemId: String,
      request: UpdateMenuItemRequest,
    ): Response
  }

  interface RemoveMenuItemAPI<Response> {
    fun removeMenuItem(
      restaurantId: String,
      menuId: String,
      categoryId: String,
      itemId: String,
    ): Response
  }
}
