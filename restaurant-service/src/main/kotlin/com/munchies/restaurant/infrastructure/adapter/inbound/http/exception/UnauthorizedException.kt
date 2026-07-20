package com.munchies.restaurant.infrastructure.adapter.inbound.http.exception

class UnauthorizedException : RuntimeException {
  constructor(message: String) : super(message)
}
