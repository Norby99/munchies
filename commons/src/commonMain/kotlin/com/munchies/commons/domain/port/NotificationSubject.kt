package com.munchies.commons.domain.port

import kotlin.js.JsExport

@JsExport
interface NotificationSubject<N : Notification, O : NotificationObserver<N>> {
  fun attach(observer: O): Unit
  fun detach(observer: O): Unit
  fun emit(event: N): Unit
}
