package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.restaurant.GetTablesResult
import com.munchies.restaurant.application.usecase.restaurant.TableSpec
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesCommand
import com.munchies.restaurant.application.usecase.restaurant.UpdateTablesResult
import com.munchies.restaurant.domain.valueobject.restaurant.Table
import com.munchies.restaurant.infrastructure.adapter.dto.TableDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetTablesResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateTablesResponse

fun Table.toDto(): TableDto = TableDto(number = number, seats = seats)

fun GetTablesResult.Success.toResponse(): GetTablesResponse =
  GetTablesResponse(tables.map { it.toDto() }.toTypedArray())

fun UpdateTablesRequest.toCommand(restaurantId: String): UpdateTablesCommand = UpdateTablesCommand(
  managerId = managerId,
  restaurantId = restaurantId,
  tables = tables.map { TableSpec(number = it.number, seats = it.seats) },
)

fun UpdateTablesResult.Success.toResponse(): UpdateTablesResponse =
  UpdateTablesResponse(tables.map { it.toDto() }.toTypedArray())
