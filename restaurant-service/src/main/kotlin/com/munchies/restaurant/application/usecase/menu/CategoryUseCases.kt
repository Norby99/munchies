package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.menu.CategoryName

data class CategoryUseCases(
  val add: AddCategoryUseCase,
  val update: UpdateCategoryUseCase,
  val remove: RemoveCategoryUseCase,
)

data class AddCategoryCommand(val menuId: String, val name: String)

sealed interface AddCategoryResult {
  data class Success(val categoryId: String) : AddCategoryResult
  data object MenuNotFound : AddCategoryResult
  data class InvalidCategory(val error: String) : AddCategoryResult
}

class AddCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<AddCategoryCommand, AddCategoryResult> {
  override suspend operator fun invoke(command: AddCategoryCommand): AddCategoryResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return AddCategoryResult.MenuNotFound

    return runCatching {
      val category = menu.addCategory(CategoryName.of(command.name))
      menuRepository.save(menu)
      AddCategoryResult.Success(category.id.value)
    }.getOrElse { AddCategoryResult.InvalidCategory(it.message ?: "") }
  }
}

data class UpdateCategoryCommand(
  val menuId: String,
  val categoryId: String,
  val name: String,
)

sealed interface UpdateCategoryResult {
  data object Success : UpdateCategoryResult
  data object MenuNotFound : UpdateCategoryResult
  data class InvalidCategory(val error: String) : UpdateCategoryResult
}

class UpdateCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateCategoryCommand, UpdateCategoryResult> {
  override suspend operator fun invoke(command: UpdateCategoryCommand): UpdateCategoryResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return UpdateCategoryResult.MenuNotFound

    return runCatching {
      menu.updateCategory(CategoryId(command.categoryId), CategoryName.of(command.name))
      menuRepository.save(menu)
      UpdateCategoryResult.Success
    }.getOrElse { UpdateCategoryResult.InvalidCategory(it.message ?: "") }
  }
}

data class RemoveCategoryCommand(
  val menuId: String,
  val categoryId: String,
)

sealed interface RemoveCategoryResult {
  data object Success : RemoveCategoryResult
  data object MenuNotFound : RemoveCategoryResult
  data class InvalidCategory(val error: String) : RemoveCategoryResult
}

class RemoveCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<RemoveCategoryCommand, RemoveCategoryResult> {
  override suspend operator fun invoke(command: RemoveCategoryCommand): RemoveCategoryResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return RemoveCategoryResult.MenuNotFound

    return runCatching {
      menu.removeCategory(CategoryId(command.categoryId))
      menuRepository.save(menu)
      RemoveCategoryResult.Success
    }.getOrElse { RemoveCategoryResult.InvalidCategory(it.message ?: "") }
  }
}
