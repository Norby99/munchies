package com.munchies.order.domain.model

data class DeliveryAddress(
  val street: String,
  val city: String,
  val zipCode: String,
  val coordinates: GeoPoint?,
)

data class GeoPoint(val lat: Double, val lng: Double)
