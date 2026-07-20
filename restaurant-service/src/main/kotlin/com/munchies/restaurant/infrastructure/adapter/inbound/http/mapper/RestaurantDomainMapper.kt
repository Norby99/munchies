package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto

fun Restaurant.toDto(): RestaurantDto = RestaurantDto(
  id = id.value,
  name = name.value,
  address = address.value,
  phone = phone.value,
  email = email.value,
)
