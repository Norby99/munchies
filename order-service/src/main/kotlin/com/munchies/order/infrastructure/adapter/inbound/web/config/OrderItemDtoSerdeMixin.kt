package com.munchies.order.infrastructure.adapter.inbound.web.config

import io.micronaut.core.bind.annotation.Bindable

/**
 * Serde mixin for the shared `OrderItemDto` (imported with `@SerdeImport(mixin = ...)`).
 *
 * `unitPriceCents` is set by order-service from the restaurant menu, so clients may omit it from
 * requests, and its Kotlin default (0) covers that for kotlinx. Micronaut serde does not see
 * Kotlin default arguments of an imported class, though, and rejects a body without the field;
 * this mixin mirrors its constructor and gives the parameter the same default for Micronaut too.
 */
@Suppress("UnusedPrivateProperty")
abstract class OrderItemDtoSerdeMixin(
  val menuItemId: String,
  val quantity: Int,
  @param:Bindable(defaultValue = "0") val unitPriceCents: Int,
)
