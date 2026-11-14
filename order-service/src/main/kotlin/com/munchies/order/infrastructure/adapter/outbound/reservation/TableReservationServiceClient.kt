package com.munchies.order.infrastructure.adapter.outbound.reservation

import com.munchies.tablereservation.infrastructure.adapter.inbound.web.config.TableReservationServiceConfig
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

/**
 * Declarative Micronaut HTTP client for the table-reservation-service endpoint order-service
 * reads.
 *
 * The base URL and timeouts come from `micronaut.http.services.table-reservation-service` in
 * `application.yml`. The path and the query parameter name are reused from
 * `table-reservation-shared`, so they cannot drift from the real route. The body is returned raw
 * and decoded with the shared kotlinx parser. A `404` yields `null`.
 */
@Client(id = TableReservationServiceConfig.SERVICE_NAME)
interface TableReservationServiceClient {

  /** The reservation [id], only if it belongs to [customerId]. */
  @Get(
    TableReservationServiceConfig.SERVICE_PATH + TableReservationServiceConfig.GET_RESERVATION_PATH,
  )
  fun getReservation(
    @PathVariable id: String,
    @QueryValue(TableReservationServiceConfig.CUSTOMER_ID_QUERY) customerId: String,
  ): String?
}
