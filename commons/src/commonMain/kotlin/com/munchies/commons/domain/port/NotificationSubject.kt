package com.munchies.commons.domain.port

interface NotificationSubject<E, T : NotificationObserver<E>> {
  fun attach(observer: T)
  fun detach(observer: T)
  fun emit()
}
