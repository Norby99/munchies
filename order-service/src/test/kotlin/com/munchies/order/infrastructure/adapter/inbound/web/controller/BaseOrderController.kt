package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServices
import io.mockk.mockk

abstract class BaseOrderController {

  val advanceOrderStatus = mockk<AdvanceOrderStatus>()
  val discardOrder = mockk<DiscardOrder>()
  val getOrderDetails = mockk<GetOrderDetails>()
  val placeOrder = mockk<PlaceOrder>()
  val updateDeliveryOrderInfo = mockk<UpdateDeliveryOrderInfo>()
  val updateOrderItems = mockk<UpdateOrderItems>()
  val updateTakeawayOrderInfo = mockk<UpdateTakeawayOrderInfo>()

  val controller = MicronautOrderController(
    OrderServices(
      advanceOrderStatus,
      discardOrder,
      getOrderDetails,
      placeOrder,
      updateDeliveryOrderInfo,
      updateOrderItems,
      updateTakeawayOrderInfo,
    ),
  )
}
