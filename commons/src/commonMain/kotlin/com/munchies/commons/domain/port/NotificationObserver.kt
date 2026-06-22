package com.munchies.commons.domain.port

import kotlin.js.JsExport

@JsExport
interface NotificationObserver<N : Notification> {
  fun update(event: N): Unit
}
