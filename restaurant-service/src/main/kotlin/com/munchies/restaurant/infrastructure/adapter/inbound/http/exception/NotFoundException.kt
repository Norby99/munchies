package com.munchies.restaurant.infrastructure.adapter.inbound.http.exception

class NotFoundException : RuntimeException {
  constructor(message: String) : super(message)
}
