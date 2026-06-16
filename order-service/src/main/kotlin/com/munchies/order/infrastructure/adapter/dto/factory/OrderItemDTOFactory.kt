package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

object OrderItemDTOFactory {
  fun OrderItem.toDto(): OrderItemDto {
    return OrderItemDto(menuItemId.value, quantity)
  }

  fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(MenuItemId(menuItemId), quantity)
  }
}
