package com.munchies.order.domain.model

/**
 * Type-specific details of an order.
 *
 * Each [Order] subtype carries exactly one kind of details ([DeliveryInfo], [TakeawayInfo] or
 * [TableInfo]). Modelling them as a sealed hierarchy lets the [OrderPlaced] event carry the
 * details polymorphically, so a single creation event is enough to rebuild any order subtype.
 */
sealed interface OrderDetails
