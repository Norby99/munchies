package com.munchies.order.infrastructure.adapter.inbound.web.controller.exception

import io.micronaut.serde.annotation.Serdeable

/**
 * A dependency order-service needs to complete the request (e.g. restaurant-service, for the
 * menu prices) is unreachable. Mapped to `503 Service Unavailable`: the client may retry later.
 */
@Serdeable
class ServiceUnavailableException(msg: String) : Throwable(msg)
