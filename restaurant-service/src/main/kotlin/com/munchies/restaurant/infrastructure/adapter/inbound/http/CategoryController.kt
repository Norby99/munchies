package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.application.MenuService
import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryCommand
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper.*
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put

@Controller("/restaurant/{restaurantId}/menus/{menuId}/categories")
class CategoryController(private val menuService: MenuService) {

  @Post
  suspend fun createCategory(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @Body request: CreateCategoryRequest,
  ): HttpResponse<CreateCategoryResponse> {
    val command = request.toCommand(restaurantId, menuId)
    return when (val result = menuService.createCategory(command)) {
      is CreateCategoryResult.Success -> HttpResponse.created(result.toResponse())
      is CreateCategoryResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is CreateCategoryResult.InvalidCategory -> throw ValidationException(result.error)
    }
  }

  @Put("/{categoryId}")
  suspend fun updateCategory(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @PathVariable categoryId: String,
    @Body request: UpdateCategoryRequest,
  ): HttpResponse<UpdateCategoryResponse> {
    val command = request.toCommand(restaurantId, menuId, categoryId)
    return when (val result = menuService.updateCategory(command)) {
      is UpdateCategoryResult.Success -> HttpResponse.ok(result.toResponse())
      is UpdateCategoryResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is UpdateCategoryResult.InvalidCategory -> throw ValidationException(result.error)
    }
  }

  @Delete("/{categoryId}")
  suspend fun deleteCategory(
    @PathVariable restaurantId: String,
    @PathVariable menuId: String,
    @PathVariable categoryId: String,
  ): HttpResponse<DeleteCategoryResponse> {
    val command = DeleteCategoryCommand(restaurantId, menuId, categoryId)
    return when (val result = menuService.deleteCategory(command)) {
      is DeleteCategoryResult.Success -> HttpResponse.ok(result.toResponse())
      is DeleteCategoryResult.MenuNotFound -> throw NotFoundException("Menu not found")
      is DeleteCategoryResult.InvalidCategory -> throw ValidationException(result.error)
    }
  }
}
