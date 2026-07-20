package com.munchies.restaurant.infrastructure.adapter.inbound.http.exception

class ValidationException : RuntimeException {
  constructor(message: String) : super(message)
}
