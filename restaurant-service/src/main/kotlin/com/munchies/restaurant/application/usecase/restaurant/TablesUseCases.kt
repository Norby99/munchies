package com.munchies.restaurant.application.usecase.restaurant

import com.munchies.restaurant.application.UseCase
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Table

// --- Get Tables ---

data class GetTablesCommand(
  val restaurantId: String,
)

sealed interface GetTablesResult {
  /** [tables] is empty until the manager configures the dining room. */
  data class Success(val tables: List<Table>) : GetTablesResult
  data class InvalidRestaurant(val error: String) : GetTablesResult
  data object RestaurantNotFound : GetTablesResult
}

class GetTablesUseCase(
  private val repository: RestaurantRepository,
) : UseCase<GetTablesCommand, GetTablesResult> {

  override suspend operator fun invoke(command: GetTablesCommand): GetTablesResult =
    runCatching { RestaurantId.of(command.restaurantId) }.fold(
      onSuccess = { tablesOf(it) },
      onFailure = { GetTablesResult.InvalidRestaurant(it.message.orEmpty()) },
    )

  private suspend fun tablesOf(restaurantId: RestaurantId): GetTablesResult =
    repository.findById(restaurantId)
      ?.let { GetTablesResult.Success(it.tables) }
      ?: GetTablesResult.RestaurantNotFound
}

// --- Update Tables ---

/** A table as requested by the manager, not validated yet. */
data class TableSpec(
  val number: Int,
  val seats: Int,
)

/** Replaces the whole table layout of the restaurant with [tables]. */
data class UpdateTablesCommand(
  val managerId: String,
  val restaurantId: String,
  val tables: List<TableSpec>,
)

sealed interface UpdateTablesResult {
  data class Success(val tables: List<Table>) : UpdateTablesResult
  data class InvalidRestaurant(val error: String) : UpdateTablesResult
  data class InvalidTables(val error: String) : UpdateTablesResult
  data object NotFound : UpdateTablesResult
  data object Unauthorized : UpdateTablesResult
}

class UpdateTablesUseCase(
  private val repository: RestaurantRepository,
) : UseCase<UpdateTablesCommand, UpdateTablesResult> {

  // Inputs are validated in this order: restaurant id, caller, tables; the first invalid one
  // decides the result, before the repository is touched.
  override suspend operator fun invoke(command: UpdateTablesCommand): UpdateTablesResult {
    val restaurantId = runCatching { RestaurantId.of(command.restaurantId) }
    val managerId = runCatching { UserId.of(command.managerId) }.getOrNull()
    val tables = runCatching { command.tables.map { Table(it.number, it.seats) } }
    return when {
      restaurantId.isFailure ->
        UpdateTablesResult.InvalidRestaurant(restaurantId.exceptionOrNull()?.message.orEmpty())
      managerId == null -> UpdateTablesResult.Unauthorized
      tables.isFailure ->
        UpdateTablesResult.InvalidTables(tables.exceptionOrNull()?.message.orEmpty())
      else -> update(restaurantId.getOrThrow(), managerId, tables.getOrThrow())
    }
  }

  private suspend fun update(
    restaurantId: RestaurantId,
    managerId: UserId,
    tables: List<Table>,
  ): UpdateTablesResult {
    val restaurant = repository.findById(restaurantId)
    return when {
      restaurant == null -> UpdateTablesResult.NotFound
      restaurant.managerId != managerId -> UpdateTablesResult.Unauthorized
      // The layout as a whole is checked by the aggregate (no two tables with the same number).
      else -> runCatching { restaurant.updateTables(tables) }.fold(
        onSuccess = {
          repository.update(restaurant)
          UpdateTablesResult.Success(restaurant.tables)
        },
        onFailure = { UpdateTablesResult.InvalidTables(it.message.orEmpty()) },
      )
    }
  }
}
