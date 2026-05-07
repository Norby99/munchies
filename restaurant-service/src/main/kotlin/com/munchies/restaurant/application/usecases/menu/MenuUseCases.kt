package com.munchies.restaurant.application.usecases.menu

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.repository.MenuRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import java.time.LocalDate
import java.time.MonthDay

sealed interface ValidityConfig {
  data class Period(val start: LocalDate, val end: LocalDate) : ValidityConfig
  data class Yearly(
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int,
  ) : ValidityConfig
  data class From(val start: LocalDate) : ValidityConfig
  data class Until(val end: LocalDate) : ValidityConfig
  data object Always : ValidityConfig

  fun toDomain(): Validity = when (this) {
    is Period -> Validity.period(start, end)
    is Yearly -> Validity.yearly(MonthDay.of(startMonth, startDay), MonthDay.of(endMonth, endDay))
    is From -> Validity.from(start)
    is Until -> Validity.until(end)
    is Always -> Validity.always
  }
}

data class MenuUseCases(
  val add: AddMenuUseCase,
  val update: UpdateMenuUseCase,
  val remove: RemoveMenuUseCase,
)

data class AddMenuCommand(
  val restaurantId: String,
  val name: String,
  val validity: ValidityConfig,
)

sealed interface AddMenuResult {
  data class Success(val menuId: String) : AddMenuResult
  data class InvalidMenu(val error: String) : AddMenuResult
}

class AddMenuUseCase(
  private val menuRepository: MenuRepository,
) : UseCase<AddMenuCommand, AddMenuResult> {
  override suspend operator fun invoke(command: AddMenuCommand): AddMenuResult {
    return runCatching {
      val menu = Menu.create(
        restaurantId = RestaurantId(command.restaurantId),
        name = MenuName.of(command.name),
        validity = command.validity.toDomain(),
      )
      menuRepository.save(menu)
      AddMenuResult.Success(menu.id.value)
    }.getOrElse { AddMenuResult.InvalidMenu(it.message ?: "") }
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
