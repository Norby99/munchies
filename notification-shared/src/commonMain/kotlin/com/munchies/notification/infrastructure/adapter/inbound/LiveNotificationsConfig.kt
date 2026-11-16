package com.munchies.notification.infrastructure.adapter.inbound

import kotlin.js.JsExport

/**
 * Contract of the live-notification channel: a Socket.io (WebSocket) connection from the
 * frontend, terminated by gateway-service and proxied to notification-service.
 *
 * The gateway authenticates the connection with the session cookie, then forwards the caller's
 * identity to notification-service in the headers below. notification-service trusts only these
 * headers: it is not reachable from outside the cluster, and the gateway overwrites any value a
 * client might send itself. The frontend does not use this module (it is not built against the
 * `-shared` packages); it mirrors [PATH] and [EVENT] in its own code.
 */
@JsExport
object LiveNotificationsConfig {
  /** Path of the Socket.io endpoint, the same on the gateway and on notification-service. */
  const val PATH = "/notifications"

  /** Id of the authenticated user. */
  const val USER_ID_HEADER = "x-munchies-user-id"

  /** Role of the authenticated user (`CUSTOMER` or `MANAGER`). */
  const val USER_ROLE_HEADER = "x-munchies-user-role"

  /** Comma-separated ids of the restaurants a manager owns; absent for a customer. */
  const val RESTAURANT_IDS_HEADER = "x-munchies-restaurant-ids"

  /** Name of the Socket.io event every live notification is emitted as. */
  const val EVENT = "notification"
}
