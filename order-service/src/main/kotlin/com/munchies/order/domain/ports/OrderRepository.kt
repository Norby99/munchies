package com.munchies.order.domain.ports

import com.munchies.commons.Repository
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId

/**
 * Repository interface for managing Order entities.
 *
 * @see com.munchies.commons.Repository
 */
interface OrderRepository : Repository<OrderId, Order>
