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

data class ItemVariationUseCases(
  val add: AddItemVariationUseCase,
  val update: UpdateItemVariationUseCase,
  val remove: RemoveItemVariationUseCase,
)

data class AddItemVariationCommand(
  val menuId: String,
  val categoryId: String,
  val itemId: String,
  val name: String,
  val options: List<VariationOptionDto>,
)

sealed interface AddItemVariationResult {
  data class Success(val variationId: String) : AddItemVariationResult
  data object MenuNotFound : AddItemVariationResult
  data object CategoryNotFound : AddItemVariationResult
  data object ItemNotFound : AddItemVariationResult
  data class InvalidVariation(val error: String) : AddItemVariationResult
}

class AddItemVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<AddItemVariationCommand, AddItemVariationResult> {
  override suspend operator fun invoke(command: AddItemVariationCommand): AddItemVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return AddItemVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return AddItemVariationResult.CategoryNotFound

    val item = category.items.find { it.id.value == command.itemId }
      ?: return AddItemVariationResult.ItemNotFound

    return runCatching {
      val options = command.options.map { dto ->
        VariationOption(
          name = VariationOptionName.of(dto.name),
          additionalPrice = Money(dto.additionalPrice),
        )
      }
      val variation = item.addVariation(VariationName.of(command.name), options)
      menuRepository.save(menu)
      AddItemVariationResult.Success(variation.id.value)
    }.getOrElse { AddItemVariationResult.InvalidVariation(it.message ?: "") }
  }
}

data class UpdateItemVariationCommand(
  val menuId: String,
  val categoryId: String,
  val itemId: String,
  val variationId: String,
  val name: String,
  val options: List<VariationOptionDto>,
)

sealed interface UpdateItemVariationResult {
  data object Success : UpdateItemVariationResult
  data object MenuNotFound : UpdateItemVariationResult
  data object CategoryNotFound : UpdateItemVariationResult
  data object ItemNotFound : UpdateItemVariationResult
  data class InvalidVariation(val error: String) : UpdateItemVariationResult
}

class UpdateItemVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateItemVariationCommand, UpdateItemVariationResult> {
  override suspend operator fun invoke(
    command: UpdateItemVariationCommand,
  ): UpdateItemVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return UpdateItemVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return UpdateItemVariationResult.CategoryNotFound

    val item = category.items.find { it.id.value == command.itemId }
      ?: return UpdateItemVariationResult.ItemNotFound

    return runCatching {
      val options = command.options.map { dto ->
        VariationOption(
          name = VariationOptionName.of(dto.name),
          additionalPrice = Money(dto.additionalPrice),
        )
      }
      item.updateVariation(
        VariationId(command.variationId),
        VariationName.of(command.name),
        options,
      )
      menuRepository.save(menu)
      UpdateItemVariationResult.Success
    }.getOrElse { UpdateItemVariationResult.InvalidVariation(it.message ?: "") }
  }
}

data class RemoveItemVariationCommand(
  val menuId: String,
  val categoryId: String,
  val itemId: String,
  val variationId: String,
)

sealed interface RemoveItemVariationResult {
  data object Success : RemoveItemVariationResult
  data object MenuNotFound : RemoveItemVariationResult
  data object CategoryNotFound : RemoveItemVariationResult
  data object ItemNotFound : RemoveItemVariationResult
  data class InvalidVariation(val error: String) : RemoveItemVariationResult
}

class RemoveItemVariationUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<RemoveItemVariationCommand, RemoveItemVariationResult> {
  override suspend operator fun invoke(
    command: RemoveItemVariationCommand,
  ): RemoveItemVariationResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return RemoveItemVariationResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return RemoveItemVariationResult.CategoryNotFound

    val item = category.items.find { it.id.value == command.itemId }
      ?: return RemoveItemVariationResult.ItemNotFound

    return runCatching {
      item.removeVariation(VariationId(command.variationId))
      menuRepository.save(menu)
      RemoveItemVariationResult.Success
    }.getOrElse { RemoveItemVariationResult.InvalidVariation(it.message ?: "") }
  }
}
