package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import jakarta.inject.Singleton

@Singleton
data class MenuUseCases(val repository: MenuRepository) {
  val create = CreateMenuUseCase(repository)
  val update = UpdateMenuUseCase(repository)
  val delete = DeleteMenuUseCase(repository)
  val getMenu = GetMenuUseCase(repository)
  val getRestaurantMenus = GetRestaurantMenusUseCase(repository)
}

data class GetMenuCommand(
  val restaurantId: String,
  val menuId: String,
)

sealed interface GetMenuResult {
  data class Success(val menu: Menu) : GetMenuResult
  data object MenuNotFound : GetMenuResult
}

class GetMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<GetMenuCommand, GetMenuResult> {
  override suspend operator fun invoke(command: GetMenuCommand): GetMenuResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    ) ?: return GetMenuResult.MenuNotFound
    return GetMenuResult.Success(menu)
  }
}

data class GetRestaurantMenusCommand(val restaurantId: String)

sealed interface GetRestaurantMenusResult {
  data class Success(val menus: List<Menu>) : GetRestaurantMenusResult
}

class GetRestaurantMenusUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<GetRestaurantMenusCommand, GetRestaurantMenusResult> {
  override suspend fun invoke(command: GetRestaurantMenusCommand): GetRestaurantMenusResult {
    val menus = menuRepository.findAllByRestaurantId(RestaurantId(command.restaurantId))

    return GetRestaurantMenusResult.Success(menus)
  }
}

data class CreateMenuCommand(
  val restaurantId: String,
  val name: String,
  val validity: ValidityInput,
)

sealed interface CreateMenuResult {
  data class Success(val menu: Menu) : CreateMenuResult
  data class InvalidMenu(val error: String) : CreateMenuResult
}

class CreateMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<CreateMenuCommand, CreateMenuResult> {
  override suspend operator fun invoke(command: CreateMenuCommand): CreateMenuResult {
    return runCatching {
      val menu = Menu.create(
        restaurantId = RestaurantId(command.restaurantId),
        name = MenuName.of(command.name),
        validity = command.validity.toDomain(),
      )
      menuRepository.save(menu)
      CreateMenuResult.Success(menu)
    }.getOrElse { CreateMenuResult.InvalidMenu(it.message.orEmpty()) }
  }
}

data class UpdateMenuCommand(
  val restaurantId: String,
  val menuId: String,
  val name: String,
  val validity: ValidityInput,
)

sealed interface UpdateMenuResult {
  data class Success(val menu: Menu) : UpdateMenuResult
  data object MenuNotFound : UpdateMenuResult
  data class InvalidMenu(val error: String) : UpdateMenuResult
}

class UpdateMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateMenuCommand, UpdateMenuResult> {
  override suspend operator fun invoke(command: UpdateMenuCommand): UpdateMenuResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return UpdateMenuResult.MenuNotFound

    return runCatching {
      menu.updateName(MenuName.of(command.name))
      menu.updateValidity(command.validity.toDomain())
      menuRepository.save(menu)
      UpdateMenuResult.Success(menu)
    }.getOrElse { UpdateMenuResult.InvalidMenu(it.message.orEmpty()) }
  }
}

data class DeleteMenuCommand(
  val restaurantId: String,
  val menuId: String,
)

sealed interface DeleteMenuResult {
  data class Success(val menuId: String) : DeleteMenuResult
  data object MenuNotFound : DeleteMenuResult
  data class InvalidMenu(val error: String) : DeleteMenuResult
}

class DeleteMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<DeleteMenuCommand, DeleteMenuResult> {
  override suspend operator fun invoke(command: DeleteMenuCommand): DeleteMenuResult {
    val menu = menuRepository.findByIdAndRestaurantId(
      MenuId(command.menuId),
      RestaurantId(command.restaurantId),
    )
      ?: return DeleteMenuResult.MenuNotFound

    return runCatching {
      menuRepository.delete(menu.id)
      DeleteMenuResult.Success(menu.id.value)
    }.getOrElse { DeleteMenuResult.InvalidMenu(it.message.orEmpty()) }
  }
}
