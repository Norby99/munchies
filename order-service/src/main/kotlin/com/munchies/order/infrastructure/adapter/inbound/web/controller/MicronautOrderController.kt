package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.micronaut.http.annotation.Controller

@Controller(
  port = OrderServiceConfig.SERVICE_PORT.toString(),
  value = OrderServiceConfig.SERVICE_PATH,
)
class MicronautOrderController
