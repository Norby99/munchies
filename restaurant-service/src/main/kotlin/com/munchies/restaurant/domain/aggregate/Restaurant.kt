package com.munchies.restaurant.domain.aggregate

import com.munchies.commons.AggregateRoot
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import java.time.LocalDateTime

data class RestaurantDetails(
  val name: RestaurantName,
  val address: Address,
  val phone: Phone,
  val email: Email,
)

class Restaurant private constructor(
  override val id: RestaurantId,
  val managerId: UserId,
  var details: RestaurantDetails,
  var capacityPolicy: CapacityPolicy?,
  var location: GeoPoint?,
  val createdAt: LocalDateTime,
  var updatedAt: LocalDateTime,
) : AggregateRoot<RestaurantId>(id) {

  val name get(): RestaurantName = details.name
  val address get(): Address = details.address
  val phone get(): Phone = details.phone
  val email get(): Email = details.email

  fun updateCapacityPolicy(newPolicy: CapacityPolicy) {
    this.capacityPolicy = newPolicy
    this.updatedAt = LocalDateTime.now()
  }

  fun updateLocation(newLocation: GeoPoint) {
    this.location = newLocation
    this.updatedAt = LocalDateTime.now()
  }

  fun updateDetails(newDetails: RestaurantDetails) {
    this.details = newDetails
    this.updatedAt = LocalDateTime.now()
  }

  fun updateName(newName: RestaurantName) {
    this.details = details.copy(name = newName)
    this.updatedAt = LocalDateTime.now()
  }

  fun updateAddress(newAddress: Address) {
    this.details = details.copy(address = newAddress)
    this.updatedAt = LocalDateTime.now()
  }

  fun updatePhone(newPhone: Phone) {
    this.details = details.copy(phone = newPhone)
    this.updatedAt = LocalDateTime.now()
  }

  fun updateEmail(newEmail: Email) {
    this.details = details.copy(email = newEmail)
    this.updatedAt = LocalDateTime.now()
  }

  companion object {
    fun create(managerId: UserId, details: RestaurantDetails): Restaurant {
      val now = LocalDateTime.now()
      return Restaurant(
        id = RestaurantId(),
        details = details,
        managerId = managerId,
        capacityPolicy = null,
        location = null,
        createdAt = now,
        updatedAt = now,
      )
    }

    fun create(
      managerId: UserId,
      name: RestaurantName,
      address: Address,
      phone: Phone,
      email: Email,
    ): Restaurant {
      val details = RestaurantDetails(name = name, address = address, phone = phone, email = email)
      return create(managerId, details)
    }

    fun fromDatabase(
      id: RestaurantId,
      managerId: UserId,
      details: RestaurantDetails,
      createdAt: LocalDateTime,
      updatedAt: LocalDateTime,
      capacityPolicy: CapacityPolicy? = null,
      location: GeoPoint? = null,
    ): Restaurant {
      return Restaurant(
        id = id,
        managerId = managerId,
        details = details,
        capacityPolicy = capacityPolicy,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt,
      )
    }
  }
}
