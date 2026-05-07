package com.munchies.restaurant.application

import com.munchies.restaurant.application.usecases.menu.AddCategoryCommand
import com.munchies.restaurant.application.usecases.menu.AddCategoryResult
import com.munchies.restaurant.application.usecases.menu.AddMenuItemCommand
import com.munchies.restaurant.application.usecases.menu.AddMenuItemResult
import com.munchies.restaurant.application.usecases.menu.AddVariationCommand
import com.munchies.restaurant.application.usecases.menu.AddVariationResult
import com.munchies.restaurant.application.usecases.menu.CategoryUseCases
import com.munchies.restaurant.application.usecases.menu.MenuItemUseCases
import com.munchies.restaurant.application.usecases.menu.RemoveCategoryCommand
import com.munchies.restaurant.application.usecases.menu.RemoveCategoryResult
import com.munchies.restaurant.application.usecases.menu.RemoveMenuItemCommand
import com.munchies.restaurant.application.usecases.menu.RemoveMenuItemResult
import com.munchies.restaurant.application.usecases.menu.RemoveVariationCommand
import com.munchies.restaurant.application.usecases.menu.RemoveVariationResult
import com.munchies.restaurant.application.usecases.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecases.menu.UpdateCategoryResult
import com.munchies.restaurant.application.usecases.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecases.menu.UpdateMenuItemResult
import com.munchies.restaurant.application.usecases.menu.UpdateVariationCommand
import com.munchies.restaurant.application.usecases.menu.UpdateVariationResult
import com.munchies.restaurant.application.usecases.menu.VariationUseCases

interface MenuService {
  suspend fun addCategory(command: AddCategoryCommand): AddCategoryResult
  suspend fun updateCategory(command: UpdateCategoryCommand): UpdateCategoryResult
  suspend fun removeCategory(command: RemoveCategoryCommand): RemoveCategoryResult
  suspend fun addMenuItem(command: AddMenuItemCommand): AddMenuItemResult
  suspend fun updateMenuItem(command: UpdateMenuItemCommand): UpdateMenuItemResult
  suspend fun removeMenuItem(command: RemoveMenuItemCommand): RemoveMenuItemResult
  suspend fun addVariation(command: AddVariationCommand): AddVariationResult
  suspend fun updateVariation(command: UpdateVariationCommand): UpdateVariationResult
  suspend fun removeVariation(command: RemoveVariationCommand): RemoveVariationResult
}

internal class MenuApplicationService(
  private val categoryUseCases: CategoryUseCases,
  private val menuItemUseCases: MenuItemUseCases,
  private val variationUseCases: VariationUseCases,
) : MenuService {

  override suspend fun addCategory(command: AddCategoryCommand): AddCategoryResult {
    return categoryUseCases.add(command)
  }

  override suspend fun updateCategory(command: UpdateCategoryCommand): UpdateCategoryResult {
    return categoryUseCases.update(command)
  }

  override suspend fun removeCategory(command: RemoveCategoryCommand): RemoveCategoryResult {
    return categoryUseCases.remove(command)
  }

  override suspend fun addMenuItem(command: AddMenuItemCommand): AddMenuItemResult {
    return menuItemUseCases.add(command)
  }

  override suspend fun updateMenuItem(command: UpdateMenuItemCommand): UpdateMenuItemResult {
    return menuItemUseCases.update(command)
  }

  override suspend fun removeMenuItem(command: RemoveMenuItemCommand): RemoveMenuItemResult {
    return menuItemUseCases.remove(command)
  }

  override suspend fun addVariation(command: AddVariationCommand): AddVariationResult {
    return variationUseCases.add(command)
  }

  override suspend fun updateVariation(command: UpdateVariationCommand): UpdateVariationResult {
    return variationUseCases.update(command)
  }

  override suspend fun removeVariation(command: RemoveVariationCommand): RemoveVariationResult {
    return variationUseCases.remove(command)
  }
}
