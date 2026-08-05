package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse
import java.math.BigDecimal

fun CreateMenuItemRequest.toCommand(
  restaurantId: String,
  menuId: String,
  categoryId: String,
): CreateMenuItemCommand = CreateMenuItemCommand(
  restaurantId = restaurantId,
  menuId = menuId,
  categoryId = categoryId,
  name = name,
  description = description,
  price = BigDecimal(price),
  variations = variations.map { it.toInput() },
)

fun CreateMenuItemResult.Success.toResponse(): CreateMenuItemResponse =
  CreateMenuItemResponse(itemId)

fun UpdateMenuItemRequest.toCommand(
  restaurantId: String,
  menuId: String,
  categoryId: String,
  itemId: String,
): UpdateMenuItemCommand = UpdateMenuItemCommand(
  restaurantId = restaurantId,
  menuId = menuId,
  categoryId = categoryId,
  itemId = itemId,
  name = name,
  description = description,
  price = BigDecimal(price),
  variations = variations.map { it.toInput() },
)

fun UpdateMenuItemResult.Success.toResponse(): UpdateMenuItemResponse =
  UpdateMenuItemResponse(menuItem.toDto())
