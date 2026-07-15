package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

/**
 * Factory object for converting between OrderItem domain model and OrderItemDto.
 *
 * This factory provides methods to convert an OrderItem to its corresponding DTO representation
 * and vice versa. It helps in separating the domain model from the data transfer object used
 * for communication with external systems or layers.
 */
object OrderItemDtoFactory {

  /**
   * Converts an OrderItem domain model to its corresponding OrderItemDto.
   *
   * @receiver The OrderItem instance to be converted.
   * @return The corresponding OrderItemDto representation.
   */
  fun OrderItem.toDto(): OrderItemDto {
    return OrderItemDto(menuItemId.value, quantity)
  }

  /**
   * Converts an OrderItemDto to its corresponding OrderItem domain model.
   *
   * @receiver The OrderItemDto instance to be converted.
   * @return The corresponding OrderItem domain model.
   */
  fun OrderItemDto.toDomain(): OrderItem {
    return OrderItem(MenuItemId(menuItemId), quantity)
  }
}
