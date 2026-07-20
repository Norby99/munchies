package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.*
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put

@Controller("/restaurant/{restaurantId}/menus/{menuId}/categories/{categoryId}/items")
class MenuItemController(private val menuService: MenuService) {

  @Post
  suspend fun createMenuItem(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @PathVariable categoryId: String,
    @Body request: CreateMenuItemRequest,
  ): HttpResponse<CreateMenuItemResponse> {
    val command = request.toCommand(restaurantId, menuId, categoryId)
    return when (val result = menuService.createMenuItem(command)) {
      is CreateMenuItemResult.Success -> HttpResponse.created(result.toResponse())
      is CreateMenuItemResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is CreateMenuItemResult.CategoryNotFound -> throw NotFoundException("Category not found")
      is CreateMenuItemResult.InvalidItem -> throw ValidationException(result.error)
    }
  }

  @Put("/{itemId}")
  suspend fun updateMenuItem(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @PathVariable categoryId: String,
    @PathVariable itemId: String,
    @Body request: UpdateMenuItemRequest,
  ): HttpResponse<UpdateMenuItemResponse> {
    val command = request.toCommand(restaurantId, menuId, categoryId, itemId)
    return when (val result = menuService.updateMenuItem(command)) {
      is UpdateMenuItemResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateMenuItemResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is UpdateMenuItemResult.CategoryNotFound -> throw NotFoundException("Category not found")
      is UpdateMenuItemResult.InvalidItem -> throw ValidationException(result.error)
    }
  }

  @Delete("/{itemId}")
  suspend fun removeMenuItem(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @PathVariable categoryId: String,
    @PathVariable itemId: String,
  ): HttpResponse<DeleteMenuItemResponse> {
    val command = RemoveMenuItemCommand(restaurantId, menuId, categoryId, itemId)
    return when (val result = menuService.removeMenuItem(command)) {
      is RemoveMenuItemResult.Success -> HttpResponse.ok(DeleteMenuItemResponse(itemId))
      is RemoveMenuItemResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is RemoveMenuItemResult.CategoryNotFound -> throw NotFoundException("Category not found")
      is RemoveMenuItemResult.InvalidItem -> throw ValidationException(result.error)
    }
  }
}
