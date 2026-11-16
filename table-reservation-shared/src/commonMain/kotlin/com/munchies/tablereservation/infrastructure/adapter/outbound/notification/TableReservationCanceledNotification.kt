package com.munchies.tablereservation.infrastructure.adapter.outbound.notification

import com.munchies.commons.domain.port.Notification
import com.munchies.commons.domain.port.NotificationObserver
import com.munchies.commons.domain.port.NotificationSubject
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Published when a reservation has been canceled. Carries the reservation as it was, so the
 * notification can still say what was canceled.
 *
 * Event-carried state transfer: the payload holds everything notification-service needs to
 * notify the customer and the restaurant, so it never calls back into
 * table-reservation-service.
 *
 * @property reservationId The reservation the event is about.
 * @property restaurantId The restaurant the table belongs to.
 * @property customerId The customer who made the reservation.
 * @property start Beginning of the reserved time slot, as an ISO-8601 UTC instant.
 * @property end End of the reserved time slot, as an ISO-8601 UTC instant.
 * @property numberOfGuests How many people the table is reserved for.
 * @property tableNumber The table booked for the reservation.
 */
@JsExport
@Serializable
data class TableReservationCanceledNotification(
  val reservationId: String,
  val restaurantId: String,
  val customerId: String,
  val start: String,
  val end: String,
  val numberOfGuests: Int,
  val tableNumber: Int,
) : Notification {
  fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun tableReservationCanceledNotificationFromJson(
  json: String,
): TableReservationCanceledNotification = Json.decodeFromString(json)

@JsExport
abstract class TableReservationCanceledNotificationObserver :
  NotificationObserver<TableReservationCanceledNotification> {
  abstract override fun update(event: TableReservationCanceledNotification): Unit
}

@JsExport
abstract class TableReservationCanceledNotificationSubject :
  NotificationSubject<
    TableReservationCanceledNotification,
    TableReservationCanceledNotificationObserver,
    > {
  abstract override fun attach(observer: TableReservationCanceledNotificationObserver): Unit
  abstract override fun detach(observer: TableReservationCanceledNotificationObserver): Unit
  abstract override fun emit(event: TableReservationCanceledNotification): Unit
}
