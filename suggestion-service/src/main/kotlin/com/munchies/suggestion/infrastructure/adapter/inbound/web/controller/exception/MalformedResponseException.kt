package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception

import io.micronaut.serde.annotation.Serdeable

@Serdeable
class MalformedResponseException(msg: String) : Throwable(msg)
