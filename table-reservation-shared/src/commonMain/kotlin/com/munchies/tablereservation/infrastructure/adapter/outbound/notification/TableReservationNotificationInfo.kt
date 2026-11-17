package com.munchies.tablereservation.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

/**
 * Kafka topics, and the consumer groups notification-service reads them with, of the
 * notification-bound events of a table reservation: one topic per event.
 */
@JsExport
object TableReservationNotificationInfo {
  const val TABLE_RESERVATION_ACCEPTED_TOPIC = "table_reservation_accepted_topic"
  const val TABLE_RESERVATION_ACCEPTED_GROUP_ID = "table_reservation_accepted_group_id"
  const val TABLE_RESERVATION_MODIFIED_TOPIC = "table_reservation_modified_topic"
  const val TABLE_RESERVATION_MODIFIED_GROUP_ID = "table_reservation_modified_group_id"
  const val TABLE_RESERVATION_CANCELED_TOPIC = "table_reservation_canceled_topic"
  const val TABLE_RESERVATION_CANCELED_GROUP_ID = "table_reservation_canceled_group_id"
}
