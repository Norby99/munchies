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
 * Published when a reservation has been moved to another time slot and/or changed its number
 * of guests. Carries the reservation as it is after the change.
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
data class TableReservationModifiedNotification(
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
fun tableReservationModifiedNotificationFromJson(
  json: String,
): TableReservationModifiedNotification = Json.decodeFromString(json)

@JsExport
abstract class TableReservationModifiedNotificationObserver :
  NotificationObserver<TableReservationModifiedNotification> {
  abstract override fun update(event: TableReservationModifiedNotification): Unit
}

@JsExport
abstract class TableReservationModifiedNotificationSubject :
  NotificationSubject<
    TableReservationModifiedNotification,
    TableReservationModifiedNotificationObserver,
    > {
  abstract override fun attach(observer: TableReservationModifiedNotificationObserver): Unit
  abstract override fun detach(observer: TableReservationModifiedNotificationObserver): Unit
  abstract override fun emit(event: TableReservationModifiedNotification): Unit
}
