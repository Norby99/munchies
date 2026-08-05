package com.munchies.user.infrastructure.adapter.inbound.web.controller.exception

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class UnauthorizedException(msg: String) : Throwable(msg)
