package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.RestaurantDocument

object RestaurantDocumentFactory {

  fun Restaurant.toDocument(): RestaurantDocument = RestaurantDocument(
    id = id.value,
    managerId = managerId.value,
    name = details.name.value,
    address = details.address.value,
    phone = details.phone.value,
    email = details.email.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

  fun RestaurantDocument.toDomain(): Restaurant = Restaurant.fromDatabase(
    id = RestaurantId(id),
    managerId = UserId(managerId),
    details = RestaurantDetails(
      name = RestaurantName.of(name),
      address = Address.of(address),
      phone = Phone.of(phone),
      email = Email.of(email),
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
  )
}
