package com.munchies.restaurant.application.usecase.menu
import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.menu.CategoryName

data class CategoryUseCases(val repository: MenuRepository) {
  val add = CreateCategoryUseCase(repository)
  val update = UpdateCategoryUseCase(repository)
  val remove = RemoveCategoryUseCase(repository)
}

data class CreateCategoryCommand(
  val menuId: String,
  val name: String,
  val variations: List<VariationDto> = emptyList(),
)

sealed interface CreateCategoryResult {
  data class Success(val categoryId: String) : CreateCategoryResult
  data object MenuNotFound : CreateCategoryResult
  data class InvalidCategory(val error: String) : CreateCategoryResult
}

class CreateCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<CreateCategoryCommand, CreateCategoryResult> {
  override suspend operator fun invoke(command: CreateCategoryCommand): CreateCategoryResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return CreateCategoryResult.MenuNotFound

    return runCatching {
      val variations = command.variations.map { it.toDomain() }
      val category = menu.createCategory(CategoryName.of(command.name), variations)
      menuRepository.save(menu)
      CreateCategoryResult.Success(category.id.value)
    }.getOrElse { CreateCategoryResult.InvalidCategory(it.message ?: "") }
  }
}

data class UpdateCategoryCommand(
  val menuId: String,
  val categoryId: String,
  val name: String,
  val variations: List<VariationDto> = emptyList(),
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
      menu.updateCategory(
        CategoryId(command.categoryId),
        CategoryName.of(command.name),
        command.variations.map { it.toDomain() },
      )
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
