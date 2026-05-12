package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.VariationId
import com.munchies.restaurant.domain.aggregate.VariationOption
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import java.math.BigDecimal

data class VariationOptionDto(val name: String, val additionalPrice: BigDecimal)

data class VariationUseCases(
  val add: AddVariationUseCase,
  val update: UpdateVariationUseCase,
  val remove: RemoveVariationUseCase,
)

data class AddVariationCommand(
  val menuId: String,
  val categoryId: String,
  val name: String,
  val options: List<VariationOptionDto>,
)

sealed interface AddVariationResult {
  data class Success(val variationId: String) : AddVariationResult
  data object MenuNotFound : AddVariationResult
  data object CategoryNotFound : AddVariationResult
  data class InvalidVariation(val error: String) : AddVariationResult
}

class AddVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<AddVariationCommand, AddVariationResult> {
  override suspend operator fun invoke(command: AddVariationCommand): AddVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return AddVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return AddVariationResult.CategoryNotFound

    return runCatching {
      val internalOptions = command.options.map {
        VariationOption(VariationOptionName.of(it.name), Money(it.additionalPrice))
      }

      val variation = category.addVariation(VariationName.of(command.name), internalOptions)
      menuRepository.save(menu)
      AddVariationResult.Success(variation.id.value)
    }.getOrElse { AddVariationResult.InvalidVariation(it.message ?: "") }
  }
}

data class UpdateVariationCommand(
  val menuId: String,
  val categoryId: String,
  val variationId: String,
  val name: String,
  val options: List<VariationOptionDto>,
)

sealed interface UpdateVariationResult {
  data object Success : UpdateVariationResult
  data object MenuNotFound : UpdateVariationResult
  data object CategoryNotFound : UpdateVariationResult
  data class InvalidVariation(val error: String) : UpdateVariationResult
}

class UpdateVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateVariationCommand, UpdateVariationResult> {
  override suspend operator fun invoke(command: UpdateVariationCommand): UpdateVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return UpdateVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return UpdateVariationResult.CategoryNotFound

    return runCatching {
      val internalOptions = command.options.map {
        VariationOption(VariationOptionName.of(it.name), Money(it.additionalPrice))
      }

      category.updateVariation(
        VariationId(command.variationId),
        VariationName.of(command.name),
        internalOptions,
      )
      menuRepository.save(menu)
      UpdateVariationResult.Success
    }.getOrElse { UpdateVariationResult.InvalidVariation(it.message ?: "") }
  }
}

data class RemoveVariationCommand(
  val menuId: String,
  val categoryId: String,
  val variationId: String,
)

sealed interface RemoveVariationResult {
  data object Success : RemoveVariationResult
  data object MenuNotFound : RemoveVariationResult
  data object CategoryNotFound : RemoveVariationResult
  data class InvalidVariation(val error: String) : RemoveVariationResult
}

class RemoveVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<RemoveVariationCommand, RemoveVariationResult> {
  override suspend operator fun invoke(command: RemoveVariationCommand): RemoveVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return RemoveVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return RemoveVariationResult.CategoryNotFound

    return runCatching {
      category.removeVariation(VariationId(command.variationId))
      menuRepository.save(menu)
      RemoveVariationResult.Success
    }.getOrElse { RemoveVariationResult.InvalidVariation(it.message ?: "") }
  }
}
