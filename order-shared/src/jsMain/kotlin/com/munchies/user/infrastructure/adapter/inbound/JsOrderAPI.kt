package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI.*
import com.munchies.order.infrastructure.adapter.inbound.request.*

@JsExport
abstract class JsAdvanceOrderStatusAPI :
  AdvanceOrderStatusAPI<AdvanceOrderStatusRequest, OrderDto>

@JsExport
abstract class JsDiscardOrderAPI : DiscardOrderAPI<String, OrderDto>

@JsExport
abstract class JsGetOrderDetailsAPI : GetOrderDetailsAPI<String, OrderDto>

@JsExport
abstract class JsPlaceOrderAPI : PlaceOrderAPI<PlaceOrderRequest, OrderDto>

@JsExport
abstract class JsUpdateDeliveryOrderInfoAPI :
  UpdateDeliveryOrderInfoAPI<UpdateDeliveryOrderRequest, OrderDto>

@JsExport
abstract class JsUpdateOrderItemsAPI : UpdateOrderItemsAPI<UpdateOrderItemsRequest, OrderDto>

@JsExport
abstract class JsUpdateTakeawayOrderInfoAPI :
  UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderRequest, OrderDto>
