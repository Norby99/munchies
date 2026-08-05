package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.CreateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse

// --- Create ---
fun CreateCategoryRequest.toCommand(restaurantId: String, menuId: String): CreateCategoryCommand =
  CreateCategoryCommand(restaurantId, menuId, name, variations.map { it.toInput() })

fun CreateCategoryResult.Success.toResponse(): CreateCategoryResponse =
  CreateCategoryResponse(category.toDto())

fun UpdateCategoryRequest.toCommand(
  restaurantId: String,
  menuId: String,
  categoryId: String,
): UpdateCategoryCommand =
  UpdateCategoryCommand(restaurantId, menuId, categoryId, name, variations.map { it.toInput() })

fun UpdateCategoryResult.Success.toResponse(): UpdateCategoryResponse =
  UpdateCategoryResponse(category.toDto())

fun DeleteCategoryResult.Success.toResponse(): DeleteCategoryResponse =
  DeleteCategoryResponse(categoryId)
