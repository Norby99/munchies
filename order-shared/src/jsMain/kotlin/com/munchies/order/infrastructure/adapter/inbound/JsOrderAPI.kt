package com.munchies.order.infrastructure.adapter.inbound

import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse
import kotlin.js.Promise

class JsOrderAPI

@JsExport
abstract class JsAdvanceOrderStatusAPI :
  OrderAPI.AdvanceOrderStatusAPI<Promise<AdvanceOrderStatusResponse>>
/*,  API<GetUserRequest, GetUserResponse, GetUserResult, GetUserSuccess, GetUserFailure>()()*/

@JsExport
abstract class JsDiscardOrderAPI : OrderAPI.DiscardOrderAPI<Promise<DiscardOrderResponse>>

@JsExport
abstract class JsGetOrderDetailsAPI : OrderAPI.GetOrderDetailsAPI<Promise<GetOrderDetailsResponse>>

@JsExport
abstract class JsPlaceOrderAPI : OrderAPI.PlaceOrderAPI<Promise<PlaceOrderRequest>>

@JsExport
abstract class JsUpdateDeliveryOrderInfoAPI :
  OrderAPI.UpdateDeliveryOrderInfoAPI<Promise<UpdateDeliveryOrderRequest>>

@JsExport
abstract class JsUpdateOrderItemsAPI :
  OrderAPI.UpdateOrderItemsAPI<Promise<UpdateOrderItemsRequest>>

@JsExport
abstract class JsUpdateTakeawayOrderInfoAPI :
  OrderAPI.UpdateTakeawayOrderInfoAPI<Promise<UpdateTakeawayOrderRequest>>
