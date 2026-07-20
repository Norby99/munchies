package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuCommand
import com.munchies.restaurant.application.usecase.menu.DeleteMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuCommand
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusCommand
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.*
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put

@Controller("/restaurant/{restaurantId}/menus")
class MenuController(private val menuService: MenuService) {

  @Post
  suspend fun createMenu(
    @PathVariable restaurantId: String,
    @Body request: CreateMenuRequest,
  ): HttpResponse<CreateMenuResponse> {
    val command = request.toCommand(restaurantId)
    return when (val result = menuService.createMenu(command)) {
      is CreateMenuResult.Success -> HttpResponse.created(result.toResponse())
      is CreateMenuResult.InvalidMenu -> throw ValidationException(result.error)
    }
  }

  @Get("/{menuId}")
  suspend fun getMenu(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
  ): HttpResponse<GetMenuResponse> {
    val command = GetMenuCommand(restaurantId, menuId)
    return when (val result = menuService.getMenu(command)) {
      is GetMenuResult.Success -> HttpResponse.ok(result.toResponse())
      is GetMenuResult.MenuNotFound -> throw NotFoundException("Menu not found")
    }
  }

  @Get
  suspend fun getRestaurantMenus(
    @PathVariable restaurantId: String,
  ): HttpResponse<GetRestaurantMenusResponse> {
    val command = GetRestaurantMenusCommand(restaurantId)
    return when (val result = menuService.getRestaurantMenus(command)) {
      is GetRestaurantMenusResult.Success -> HttpResponse.ok(result.toResponse())
    }
  }

  @Put("/{menuId}")
  suspend fun updateMenu(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @Body request: UpdateMenuRequest,
  ): HttpResponse<UpdateMenuResponse> {
    val command = request.toCommand(restaurantId, menuId)
    return when (val result = menuService.updateMenu(command)) {
      is UpdateMenuResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateMenuResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is UpdateMenuResult.InvalidMenu -> throw ValidationException(result.error)
    }
  }

  @Delete("/{menuId}")
  suspend fun deleteMenu(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
  ): HttpResponse<DeleteMenuResponse> {
    val command = DeleteMenuCommand(restaurantId, menuId)
    return when (val result = menuService.deleteMenu(command)) {
      is DeleteMenuResult.Success -> HttpResponse.ok(result.toResponse())
      is DeleteMenuResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is DeleteMenuResult.InvalidMenu -> throw ValidationException(result.error)
    }
  }
}
