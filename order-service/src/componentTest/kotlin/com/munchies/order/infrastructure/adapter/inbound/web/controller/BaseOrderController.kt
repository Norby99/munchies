package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.infrastructure.adapter.dto.*
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject

@SerdeImport(OrderDto::class)
@SerdeImport(OrderDto.Delivery::class)
@SerdeImport(OrderDto.Takeaway::class)
@SerdeImport(OrderDto.DineIn::class)
@SerdeImport(OrderItemDto::class)
@SerdeImport(OrderType::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(GetOrderDetailsRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(DiscardOrderRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@MicronautTest
abstract class BaseOrderController {

  @Inject
  @field:Client(
    "http://localhost:${OrderServiceConfig.SERVICE_PORT}${OrderServiceConfig.SERVICE_PATH}",
  )
  lateinit var client: HttpClient

  @Inject
  lateinit var mapper: ObjectMapper
}
