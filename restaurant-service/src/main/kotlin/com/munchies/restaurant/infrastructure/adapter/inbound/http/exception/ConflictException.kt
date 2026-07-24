package com.munchies.restaurant.infrastructure.adapter.inbound.http.exception

class ConflictException : RuntimeException {
  constructor(message: String) : super(message)
}
