package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.MenuServiceConfig
import kotlin.js.Promise

// ---- Menu ----

@JsExport
abstract class JsCreateMenuAPI :
  MenuAPI.CreateMenuAPI<Promise<CreateMenuResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun createMenu(
    restaurantId: String,
    request: CreateMenuRequest,
  ): Promise<CreateMenuResponse>
}

@JsExport
abstract class JsGetMenuAPI :
  MenuAPI.GetMenuAPI<Promise<GetMenuResponse>>, API() {
  override fun getPath(): String = MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  abstract override fun getMenu(restaurantId: String, menuId: String): Promise<GetMenuResponse>
}

@JsExport
abstract class JsGetRestaurantMenusAPI :
  MenuAPI.GetRestaurantMenusAPI<Promise<GetRestaurantMenusResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_RESTAURANT_MENUS_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  abstract override fun getRestaurantMenus(
    restaurantId: String,
  ): Promise<GetRestaurantMenusResponse>
}

@JsExport
abstract class JsUpdateMenuAPI :
  MenuAPI.UpdateMenuAPI<Promise<UpdateMenuResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  abstract override fun updateMenu(
    restaurantId: String,
    menuId: String,
    request: UpdateMenuRequest,
  ): Promise<UpdateMenuResponse>
}

@JsExport
abstract class JsDeleteMenuAPI :
  MenuAPI.DeleteMenuAPI<Promise<DeleteMenuResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.DELETE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  abstract override fun deleteMenu(
    restaurantId: String,
    menuId: String,
  ): Promise<DeleteMenuResponse>
}

// ---- Category ----

@JsExport
abstract class JsCreateCategoryAPI :
  MenuAPI.CreateCategoryAPI<Promise<CreateCategoryResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun createCategory(
    restaurantId: String,
    menuId: String,
    request: CreateCategoryRequest,
  ): Promise<CreateCategoryResponse>
}

@JsExport
abstract class JsUpdateCategoryAPI :
  MenuAPI.UpdateCategoryAPI<Promise<UpdateCategoryResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  abstract override fun updateCategory(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    request: UpdateCategoryRequest,
  ): Promise<UpdateCategoryResponse>
}

@JsExport
abstract class JsDeleteCategoryAPI :
  MenuAPI.DeleteCategoryAPI<Promise<DeleteCategoryResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.DELETE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  abstract override fun deleteCategory(
    restaurantId: String,
    menuId: String,
    categoryId: String,
  ): Promise<DeleteCategoryResponse>
}

// ---- Menu Item ----

@JsExport
abstract class JsCreateMenuItemAPI :
  MenuAPI.CreateMenuItemAPI<Promise<CreateMenuItemResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun createMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    request: CreateMenuItemRequest,
  ): Promise<CreateMenuItemResponse>
}

@JsExport
abstract class JsUpdateMenuItemAPI :
  MenuAPI.UpdateMenuItemAPI<Promise<UpdateMenuItemResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  abstract override fun updateMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    itemId: String,
    request: UpdateMenuItemRequest,
  ): Promise<UpdateMenuItemResponse>
}

@JsExport
abstract class JsRemoveMenuItemAPI :
  MenuAPI.RemoveMenuItemAPI<Promise<DeleteMenuItemResponse>>, API() {
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.REMOVE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  abstract override fun removeMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    itemId: String,
  ): Promise<DeleteMenuItemResponse>
}
