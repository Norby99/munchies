package com.munchies.tablereservation.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

/**
 * Name, port and endpoint paths of table-reservation-service, shared with its callers so they
 * cannot drift from the real routes.
 *
 * The list of reservations lives on the collection root ([SERVICE_PATH]), filtered through the
 * query parameters named by the `*_QUERY` constants.
 */
@JsExport
object TableReservationServiceConfig {
  const val SERVICE_NAME = "table-reservation-service"
  const val SERVICE_PATH = "/reservations/"
  const val SERVICE_PORT = 8080

  const val RESERVE_TABLE_PATH = ""
  const val GET_RESERVATION_PATH = "{id}/"
  const val MODIFY_RESERVATION_PATH = "{id}/"
  const val CANCEL_RESERVATION_PATH = "{id}/cancel/"

  /** Only reservations of this customer. On a single reservation: it must belong to them. */
  const val CUSTOMER_ID_QUERY = "customerId"

  /** Only reservations at this restaurant. */
  const val RESTAURANT_ID_QUERY = "restaurantId"

  /** The requesting manager: they must manage the restaurant named by [RESTAURANT_ID_QUERY]. */
  const val MANAGER_ID_QUERY = "managerId"
}
