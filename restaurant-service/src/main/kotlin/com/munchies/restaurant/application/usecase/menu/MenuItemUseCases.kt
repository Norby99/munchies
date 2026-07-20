package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
data class MenuItemUseCases(val repository: MenuRepository) {
  val add = CreateMenuItemUseCase(repository)
  val update = UpdateMenuItemUseCase(repository)
  val remove = RemoveMenuItemUseCase(repository)
}

data class CreateMenuItemCommand(
  val restaurantId: String,
  val menuId: String,
  val categoryId: String,
  val name: String,
  val description: String,
  val price: BigDecimal,
  val variations: List<VariationInput> = emptyList(),
)

sealed interface CreateMenuItemResult {
  data class Success(val itemId: String) : CreateMenuItemResult
  data object MenuNotFound : CreateMenuItemResult
  data object CategoryNotFound : CreateMenuItemResult
  data class InvalidItem(val error: String) : CreateMenuItemResult
}

class CreateMenuItemUseCase(private val menuRepository: MenuRepository) :
  UseCase<CreateMenuItemCommand, CreateMenuItemResult> {
  override suspend operator fun invoke(command: CreateMenuItemCommand): CreateMenuItemResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return CreateMenuItemResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return CreateMenuItemResult.CategoryNotFound

    return runCatching {
      val item = category.createItem(
        MenuItemDetails(
          MenuItemName.of(command.name),
          MenuItemDescription.of(command.description),
        ),
        Money(command.price),
        command.variations.map { it.toDomain() },
      )
      menuRepository.save(menu)
      CreateMenuItemResult.Success(item.id.value)
    }.getOrElse { CreateMenuItemResult.InvalidItem(it.message.orEmpty()) }
  }
}

data class UpdateMenuItemCommand(
  val restaurantId: String,
  val menuId: String,
  val categoryId: String,
  val itemId: String,
  val name: String,
  val description: String,
  val price: BigDecimal,
  val variations: List<VariationInput> = emptyList(),
)

sealed interface UpdateMenuItemResult {
  data class Success(val menuItem: MenuItem) : UpdateMenuItemResult
  data object MenuNotFound : UpdateMenuItemResult
  data object CategoryNotFound : UpdateMenuItemResult
  data class InvalidItem(val error: String) : UpdateMenuItemResult
}

class UpdateMenuItemUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateMenuItemCommand, UpdateMenuItemResult> {
  override suspend operator fun invoke(command: UpdateMenuItemCommand): UpdateMenuItemResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return UpdateMenuItemResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return UpdateMenuItemResult.CategoryNotFound

    return runCatching {
      val item = category.updateItem(
        MenuItemId(command.itemId),
        MenuItemDetails(
          MenuItemName.of(command.name),
          MenuItemDescription.of(command.description),
        ),
        Money(command.price),
        command.variations.map { it.toDomain() },
      )
      menuRepository.save(menu)
      UpdateMenuItemResult.Success(item)
    }.getOrElse { UpdateMenuItemResult.InvalidItem(it.message.orEmpty()) }
  }
}

data class RemoveMenuItemCommand(
  val restaurantId: String,
  val menuId: String,
  val categoryId: String,
  val itemId: String,
)

sealed interface RemoveMenuItemResult {
  data object Success : RemoveMenuItemResult
  data object MenuNotFound : RemoveMenuItemResult
  data object CategoryNotFound : RemoveMenuItemResult
  data class InvalidItem(val error: String) : RemoveMenuItemResult
}

class RemoveMenuItemUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<RemoveMenuItemCommand, RemoveMenuItemResult> {
  override suspend operator fun invoke(command: RemoveMenuItemCommand): RemoveMenuItemResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return RemoveMenuItemResult.MenuNotFound

    val category = menu.getCategory(CategoryId(command.categoryId))
      ?: return RemoveMenuItemResult.CategoryNotFound

    return runCatching {
      category.removeItem(MenuItemId(command.itemId))
      menuRepository.save(menu)
      RemoveMenuItemResult.Success
    }.getOrElse { RemoveMenuItemResult.InvalidItem(it.message.orEmpty()) }
  }
}
