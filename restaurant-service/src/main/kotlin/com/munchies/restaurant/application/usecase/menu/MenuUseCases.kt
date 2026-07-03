package com.munchies.restaurant.application.usecase.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity

data class MenuUseCases(val repository: MenuRepository) {
  val add = CreateMenuUseCase(repository)
  val update = UpdateMenuUseCase(repository)
  val remove = RemoveMenuUseCase(repository)
  val getDetails = GetMenuUseCase(repository)
  val getRestaurantMenus = GetRestaurantMenusUseCase(repository)
}

data class GetMenuCommand(val menuId: String)

sealed interface GetMenuResult {
  data class Success(val menu: Menu) : GetMenuResult
  data object MenuNotFound : GetMenuResult
}

class GetMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<GetMenuCommand, GetMenuResult> {
  override suspend operator fun invoke(command: GetMenuCommand): GetMenuResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return GetMenuResult.MenuNotFound
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
  val validity: ValidityConfig,
)

sealed interface CreateMenuResult {
  data class Success(val menuId: String) : CreateMenuResult
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
      CreateMenuResult.Success(menu.id.value)
    }.getOrElse { CreateMenuResult.InvalidMenu(it.message ?: "") }
  }
}

data class UpdateMenuCommand(
  val menuId: String,
  val name: String,
  val validity: ValidityConfig?,
)

sealed interface UpdateMenuResult {
  data object Success : UpdateMenuResult
  data object MenuNotFound : UpdateMenuResult
  data class InvalidMenu(val error: String) : UpdateMenuResult
}

class UpdateMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<UpdateMenuCommand, UpdateMenuResult> {
  override suspend operator fun invoke(command: UpdateMenuCommand): UpdateMenuResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return UpdateMenuResult.MenuNotFound

    return runCatching {
      menu.updateName(MenuName.of(command.name))
      if (command.validity != null) {
        menu.updateValidity(command.validity.toDomain())
      } else {
        menu.updateValidity(Validity.Always)
      }
      menuRepository.save(menu)
      UpdateMenuResult.Success
    }.getOrElse { UpdateMenuResult.InvalidMenu(it.message ?: "") }
  }
}

data class RemoveMenuCommand(val menuId: String)

sealed interface RemoveMenuResult {
  data object Success : RemoveMenuResult
  data object MenuNotFound : RemoveMenuResult
  data class InvalidMenu(val error: String) : RemoveMenuResult
}

class RemoveMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<RemoveMenuCommand, RemoveMenuResult> {
  override suspend operator fun invoke(command: RemoveMenuCommand): RemoveMenuResult {
    val menu = menuRepository.findById(MenuId(command.menuId))
      ?: return RemoveMenuResult.MenuNotFound

    return runCatching {
      menuRepository.delete(menu.id)
      RemoveMenuResult.Success
    }.getOrElse { RemoveMenuResult.InvalidMenu(it.message ?: "") }
  }
}
