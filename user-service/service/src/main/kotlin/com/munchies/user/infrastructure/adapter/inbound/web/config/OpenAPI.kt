package com.munchies.user.infrastructure.adapter.inbound.web.config

import com.munchies.user.infrastructure.adapter.inbound.web.controller.MicronautUserController
import io.micronaut.openapi.annotation.OpenAPIInclude
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
  info =
  Info(
    title = "Munchies User Service API",
    version = "1.0",
  ),
)
@OpenAPIInclude(
  classes = [
    MicronautUserController::class,
  ],
)
object OpenAPI
