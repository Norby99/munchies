package com.munchies.order.infrastructure.adapter.inbound.web.config

import com.munchies.order.infrastructure.adapter.inbound.web.controller.MicronautOrderController
import io.micronaut.openapi.annotation.OpenAPIInclude
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
  info =
  Info(
    title = "Munchies Order Service API",
    version = "1.0",
  ),
)
@OpenAPIInclude(
  classes = [
    MicronautOrderController::class,
  ],
)
object OpenAPI
