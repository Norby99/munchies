package com.munchies.order.infrastructure.adapter.inbound.web.controller.exception

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class UnexpectedException(msg: String) : Throwable(msg)
