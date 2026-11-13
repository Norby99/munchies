package com.munchies.tablereservation.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.CancelTableReservationRequest
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.ModifyTableReservationRequest
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.ReserveTableRequest
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.cancelTableReservationRequestFromJson
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.modifyTableReservationRequestFromJson
import com.munchies.tablereservation.infrastructure.adapter.inbound.request.reserveTableRequestFromJson
import com.munchies.tablereservation.infrastructure.adapter.inbound.web.config.TableReservationServiceConfig
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationResponse
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationsResponse
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.tableReservationResponseFromJson
import com.munchies.tablereservation.infrastructure.adapter.outbound.response.tableReservationsResponseFromJson
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsReserveTableAPI<E : WebResponse<Any>> :
  SimpleAPI<ReserveTableRequest, TableReservationResponse>() {
  abstract fun reserveTable(request: ReserveTableRequest): Promise<E>
  override fun getPath(): String =
    TableReservationServiceConfig.SERVICE_PATH + TableReservationServiceConfig.RESERVE_TABLE_PATH
  override fun getPort(): Int = TableReservationServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): ReserveTableRequest = reserveTableRequestFromJson(json)
  override fun parseResponse(json: String): TableReservationResponse =
    tableReservationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsGetTableReservationAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, TableReservationResponse>() {
  /** [customerId], when given, must be the customer who made the reservation. */
  abstract fun getTableReservation(id: String, customerId: String?): Promise<E>
  override fun getPath(): String = TableReservationServiceConfig.SERVICE_PATH +
    TableReservationServiceConfig.GET_RESERVATION_PATH
  override fun getPort(): Int = TableReservationServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
  override fun parseResponse(json: String): TableReservationResponse =
    tableReservationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsGetTableReservationsAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, TableReservationsResponse>() {
  /**
   * Lists the reservations of [customerId], or the ones at [restaurantId] on behalf of
   * [managerId], who must manage that restaurant.
   */
  abstract fun getTableReservations(
    customerId: String?,
    restaurantId: String?,
    managerId: String?,
  ): Promise<E>

  // The list lives on the collection root, filters travel as query parameters.
  override fun getPath(): String = TableReservationServiceConfig.SERVICE_PATH
  override fun getPort(): Int = TableReservationServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
  override fun parseResponse(json: String): TableReservationsResponse =
    tableReservationsResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsModifyTableReservationAPI<E : WebResponse<Any>> :
  SimpleAPI<ModifyTableReservationRequest, TableReservationResponse>() {
  abstract fun modifyTableReservation(
    id: String,
    request: ModifyTableReservationRequest,
  ): Promise<E>
  override fun getPath(): String = TableReservationServiceConfig.SERVICE_PATH +
    TableReservationServiceConfig.MODIFY_RESERVATION_PATH
  override fun getPort(): Int = TableReservationServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): ModifyTableReservationRequest =
    modifyTableReservationRequestFromJson(json)
  override fun parseResponse(json: String): TableReservationResponse =
    tableReservationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsCancelTableReservationAPI<E : WebResponse<Any>> :
  SimpleAPI<CancelTableReservationRequest, TableReservationResponse>() {
  abstract fun cancelTableReservation(
    id: String,
    request: CancelTableReservationRequest,
  ): Promise<E>
  override fun getPath(): String = TableReservationServiceConfig.SERVICE_PATH +
    TableReservationServiceConfig.CANCEL_RESERVATION_PATH
  override fun getPort(): Int = TableReservationServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): CancelTableReservationRequest =
    cancelTableReservationRequestFromJson(json)
  override fun parseResponse(json: String): TableReservationResponse =
    tableReservationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}
