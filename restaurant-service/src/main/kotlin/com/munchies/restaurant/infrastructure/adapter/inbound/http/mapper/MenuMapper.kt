package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateMenuCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuResult
import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse

// --- Create ---

fun CreateMenuRequest.toCommand(restaurantId: String): CreateMenuCommand =
  CreateMenuCommand(restaurantId, name, ValidityInput.Always)

fun CreateMenuResult.Success.toResponse(): CreateMenuResponse = CreateMenuResponse(menu.toDto())

// --- Get ---

fun GetMenuResult.Success.toResponse(): GetMenuResponse = GetMenuResponse(menu.toDto())

fun GetRestaurantMenusResult.Success.toResponse(): GetRestaurantMenusResponse =
  GetRestaurantMenusResponse(menus.map { it.toSummaryDto() })

// --- Update ---

fun UpdateMenuRequest.toCommand(restaurantId: String, menuId: String): UpdateMenuCommand =
  UpdateMenuCommand(restaurantId, menuId, name, validity.toInput())

fun UpdateMenuResult.Success.toResponse(): UpdateMenuResponse = UpdateMenuResponse(menu.toDto())

// --- Delete ---

fun DeleteMenuResult.Success.toResponse(): DeleteMenuResponse = DeleteMenuResponse(menuId)
