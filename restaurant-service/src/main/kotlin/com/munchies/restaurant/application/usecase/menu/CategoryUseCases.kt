package com.munchies.restaurant.application.usecase.menu
import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import jakarta.inject.Singleton

@Singleton
data class CategoryUseCases(val repository: MenuRepository) {
  val create = CreateCategoryUseCase(repository)
  val update = UpdateCategoryUseCase(repository)
  val remove = DeleteCategoryUseCase(repository)
}

data class CreateCategoryCommand(
  val restaurantId: String,
  val menuId: String,
  val name: String,
  val variations: List<VariationInput> = emptyList(),
)

sealed interface CreateCategoryResult {
  data class Success(val category: Category) : CreateCategoryResult
  data object MenuNotFound : CreateCategoryResult
  data class InvalidCategory(val error: String) : CreateCategoryResult
}

class CreateCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<CreateCategoryCommand, CreateCategoryResult> {
  override suspend operator fun invoke(command: CreateCategoryCommand): CreateCategoryResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return CreateCategoryResult.MenuNotFound

    return runCatching {
      val variations = command.variations.map { it.toDomain() }
      val category = menu.createCategory(CategoryName.of(command.name), variations)
      menuRepository.save(menu)
      CreateCategoryResult.Success(category)
    }.getOrElse { CreateCategoryResult.InvalidCategory(it.message.orEmpty()) }
  }
}

data class UpdateCategoryCommand(
  val restaurantId: String,
  val menuId: String,
  val categoryId: String,
  val name: String,
  val variations: List<VariationInput> = emptyList(),
)

sealed interface UpdateCategoryResult {
  data class Success(val category: Category) : UpdateCategoryResult
  data object MenuNotFound : UpdateCategoryResult
  data class InvalidCategory(val error: String) : UpdateCategoryResult
}

class UpdateCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateCategoryCommand, UpdateCategoryResult> {
  override suspend operator fun invoke(command: UpdateCategoryCommand): UpdateCategoryResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return UpdateCategoryResult.MenuNotFound

    return runCatching {
      val categoryId = CategoryId(command.categoryId)
      menu.updateCategory(
        categoryId,
        CategoryName.of(command.name),
        command.variations.map { it.toDomain() },
      )
      menuRepository.save(menu)
      val category = menu.getCategory(categoryId)
        ?: throw IllegalArgumentException("Category not found after update")
      UpdateCategoryResult.Success(category)
    }.getOrElse { UpdateCategoryResult.InvalidCategory(it.message.orEmpty()) }
  }
}

data class DeleteCategoryCommand(
  val restaurantId: String,
  val menuId: String,
  val categoryId: String,
)

sealed interface DeleteCategoryResult {
  data class Success(val categoryId: String) : DeleteCategoryResult
  data object MenuNotFound : DeleteCategoryResult
  data class InvalidCategory(val error: String) : DeleteCategoryResult
}

class DeleteCategoryUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<DeleteCategoryCommand, DeleteCategoryResult> {
  override suspend operator fun invoke(command: DeleteCategoryCommand): DeleteCategoryResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return DeleteCategoryResult.MenuNotFound

    return runCatching {
      menu.deleteCategory(CategoryId(command.categoryId))
      menuRepository.save(menu)
      DeleteCategoryResult.Success(command.categoryId)
    }.getOrElse { DeleteCategoryResult.InvalidCategory(it.message.orEmpty()) }
  }
}
