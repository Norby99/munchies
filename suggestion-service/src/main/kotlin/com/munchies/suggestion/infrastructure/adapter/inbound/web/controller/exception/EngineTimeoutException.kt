package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class EngineTimeoutException(msg: String) : Throwable(msg)
