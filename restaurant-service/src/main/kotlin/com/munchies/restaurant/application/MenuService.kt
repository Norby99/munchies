package com.munchies.restaurant.application
import com.munchies.restaurant.application.usecase.menu.CategoryUseCases
import com.munchies.restaurant.application.usecase.menu.CreateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.CreateCategoryResult
import com.munchies.restaurant.application.usecase.menu.CreateMenuCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.CreateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.CreateMenuResult
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryCommand
import com.munchies.restaurant.application.usecase.menu.DeleteCategoryResult
import com.munchies.restaurant.application.usecase.menu.DeleteMenuCommand
import com.munchies.restaurant.application.usecase.menu.DeleteMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.DeleteMenuResult
import com.munchies.restaurant.application.usecase.menu.GetMenuCommand
import com.munchies.restaurant.application.usecase.menu.GetMenuResult
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusCommand
import com.munchies.restaurant.application.usecase.menu.GetRestaurantMenusResult
import com.munchies.restaurant.application.usecase.menu.MenuItemUseCases
import com.munchies.restaurant.application.usecase.menu.MenuUseCases
import com.munchies.restaurant.application.usecase.menu.RemoveMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryCommand
import com.munchies.restaurant.application.usecase.menu.UpdateCategoryResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemCommand
import com.munchies.restaurant.application.usecase.menu.UpdateMenuItemResult
import com.munchies.restaurant.application.usecase.menu.UpdateMenuResult
import jakarta.inject.Singleton

interface MenuService : CategoryService, MenuItemService {
  suspend fun createMenu(command: CreateMenuCommand): CreateMenuResult
  suspend fun updateMenu(command: UpdateMenuCommand): UpdateMenuResult
  suspend fun deleteMenu(command: DeleteMenuCommand): DeleteMenuResult
  suspend fun getMenu(command: GetMenuCommand): GetMenuResult
  suspend fun getRestaurantMenus(command: GetRestaurantMenusCommand): GetRestaurantMenusResult
}

interface CategoryService {
  suspend fun createCategory(command: CreateCategoryCommand): CreateCategoryResult
  suspend fun updateCategory(command: UpdateCategoryCommand): UpdateCategoryResult
  suspend fun deleteCategory(command: DeleteCategoryCommand): DeleteCategoryResult
}

interface MenuItemService {
  suspend fun createMenuItem(command: CreateMenuItemCommand): CreateMenuItemResult
  suspend fun updateMenuItem(command: UpdateMenuItemCommand): UpdateMenuItemResult
  suspend fun deleteMenuItem(command: DeleteMenuItemCommand): RemoveMenuItemResult
}

@Singleton
class MenuApplicationService(
  private val menuUseCases: MenuUseCases,
  private val categoryUseCases: CategoryUseCases,
  private val menuItemUseCases: MenuItemUseCases,
) : MenuService,
  CategoryService by CategoryApplicationService(categoryUseCases),
  MenuItemService by MenuItemApplicationService(menuItemUseCases) {

  override suspend fun createMenu(command: CreateMenuCommand): CreateMenuResult =
    menuUseCases.create(command)
  override suspend fun updateMenu(command: UpdateMenuCommand): UpdateMenuResult =
    menuUseCases.update(command)
  override suspend fun deleteMenu(command: DeleteMenuCommand): DeleteMenuResult =
    menuUseCases.delete(command)
  override suspend fun getMenu(command: GetMenuCommand): GetMenuResult =
    menuUseCases.getMenu(command)
  override suspend fun getRestaurantMenus(
    command: GetRestaurantMenusCommand,
  ): GetRestaurantMenusResult = menuUseCases.getRestaurantMenus(command)
}

private class CategoryApplicationService(
  private val categoryUseCases: CategoryUseCases,
) : CategoryService {
  override suspend fun createCategory(command: CreateCategoryCommand): CreateCategoryResult =
    categoryUseCases.create(command)
  override suspend fun updateCategory(command: UpdateCategoryCommand): UpdateCategoryResult =
    categoryUseCases.update(command)
  override suspend fun deleteCategory(command: DeleteCategoryCommand): DeleteCategoryResult =
    categoryUseCases.delete(command)
}

private class MenuItemApplicationService(
  private val menuItemUseCases: MenuItemUseCases,
) : MenuItemService {
  override suspend fun createMenuItem(command: CreateMenuItemCommand): CreateMenuItemResult =
    menuItemUseCases.add(command)
  override suspend fun updateMenuItem(command: UpdateMenuItemCommand): UpdateMenuItemResult =
    menuItemUseCases.update(command)
  override suspend fun deleteMenuItem(command: DeleteMenuItemCommand): RemoveMenuItemResult =
    menuItemUseCases.delete(command)
}
