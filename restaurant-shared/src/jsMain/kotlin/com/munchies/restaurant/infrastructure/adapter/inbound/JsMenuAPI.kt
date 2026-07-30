package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
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
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.createCategoryRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.createCategoryResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.deleteCategoryResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.updateCategoryRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.updateCategoryResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.createMenuRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.createMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.deleteMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getRestaurantMenusResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.createMenuItemRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.createMenuItemResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.deleteMenuItemResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.updateMenuItemRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.updateMenuItemResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.updateMenuRequestFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.updateMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.MenuServiceConfig
import kotlin.js.JsExport
import kotlin.js.Promise

// ---- Menu ----

@JsExport
abstract class JsCreateMenuAPI : SimpleAPI<CreateMenuRequest, CreateMenuResponse>() {
  abstract fun createMenu(
    restaurantId: String,
    request: CreateMenuRequest,
  ): Promise<CreateMenuResponse>
  override fun parseRequest(json: String): CreateMenuRequest = createMenuRequestFromJson(json)
  override fun parseResponse(json: String): CreateMenuResponse = createMenuResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsGetMenuAPI : SimpleAPI<Nothing, GetMenuResponse>() {
  abstract fun getMenu(restaurantId: String, menuId: String): Promise<GetMenuResponse>
  override fun parseResponse(json: String): GetMenuResponse = getMenuResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String = MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsGetRestaurantMenusAPI : SimpleAPI<Nothing, GetRestaurantMenusResponse>() {
  abstract fun getRestaurantMenus(restaurantId: String): Promise<GetRestaurantMenusResponse>
  override fun parseResponse(json: String): GetRestaurantMenusResponse =
    getRestaurantMenusResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_RESTAURANT_MENUS_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateMenuAPI : SimpleAPI<UpdateMenuRequest, UpdateMenuResponse>() {
  abstract fun updateMenu(
    restaurantId: String,
    menuId: String,
    request: UpdateMenuRequest,
  ): Promise<UpdateMenuResponse>
  override fun parseRequest(json: String): UpdateMenuRequest = updateMenuRequestFromJson(json)
  override fun parseResponse(json: String): UpdateMenuResponse = updateMenuResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsDeleteMenuAPI : SimpleAPI<Nothing, DeleteMenuResponse>() {
  abstract fun deleteMenu(restaurantId: String, menuId: String): Promise<DeleteMenuResponse>
  override fun parseResponse(json: String): DeleteMenuResponse = deleteMenuResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.DELETE_MENU_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

// ---- Category ----

@JsExport
abstract class JsCreateCategoryAPI : SimpleAPI<CreateCategoryRequest, CreateCategoryResponse>() {
  abstract fun createCategory(
    restaurantId: String,
    menuId: String,
    request: CreateCategoryRequest,
  ): Promise<CreateCategoryResponse>
  override fun parseRequest(json: String): CreateCategoryRequest =
    createCategoryRequestFromJson(json)
  override fun parseResponse(json: String): CreateCategoryResponse =
    createCategoryResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsUpdateCategoryAPI : SimpleAPI<UpdateCategoryRequest, UpdateCategoryResponse>() {
  abstract fun updateCategory(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    request: UpdateCategoryRequest,
  ): Promise<UpdateCategoryResponse>
  override fun parseRequest(json: String): UpdateCategoryRequest =
    updateCategoryRequestFromJson(json)
  override fun parseResponse(json: String): UpdateCategoryResponse =
    updateCategoryResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsDeleteCategoryAPI : SimpleAPI<Nothing, DeleteCategoryResponse>() {
  abstract fun deleteCategory(
    restaurantId: String,
    menuId: String,
    categoryId: String,
  ): Promise<DeleteCategoryResponse>
  override fun parseResponse(json: String): DeleteCategoryResponse =
    deleteCategoryResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.DELETE_CATEGORY_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

// ---- Menu Item ----

@JsExport
abstract class JsCreateMenuItemAPI : SimpleAPI<CreateMenuItemRequest, CreateMenuItemResponse>() {
  abstract fun createMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    request: CreateMenuItemRequest,
  ): Promise<CreateMenuItemResponse>
  override fun parseRequest(json: String): CreateMenuItemRequest =
    createMenuItemRequestFromJson(json)
  override fun parseResponse(json: String): CreateMenuItemResponse =
    createMenuItemResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsUpdateMenuItemAPI : SimpleAPI<UpdateMenuItemRequest, UpdateMenuItemResponse>() {
  abstract fun updateMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    itemId: String,
    request: UpdateMenuItemRequest,
  ): Promise<UpdateMenuItemResponse>
  override fun parseRequest(json: String): UpdateMenuItemRequest =
    updateMenuItemRequestFromJson(json)
  override fun parseResponse(json: String): UpdateMenuItemResponse =
    updateMenuItemResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.UPDATE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsRemoveMenuItemAPI : SimpleAPI<Nothing, DeleteMenuItemResponse>() {
  abstract fun removeMenuItem(
    restaurantId: String,
    menuId: String,
    categoryId: String,
    itemId: String,
  ): Promise<DeleteMenuItemResponse>
  override fun parseResponse(json: String): DeleteMenuItemResponse =
    deleteMenuItemResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.REMOVE_MENU_ITEM_PATH
  override fun getPort(): Int = MenuServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}
