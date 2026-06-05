package com.munchies.commons.domain.port

interface NotificationObserver<E> {
  fun update(event: E)
}
