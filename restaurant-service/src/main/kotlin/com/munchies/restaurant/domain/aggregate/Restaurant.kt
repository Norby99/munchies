package com.munchies.restaurant.domain.aggregate

import com.munchies.commons.AggregateRoot
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.CapacityPolicy
import com.munchies.restaurant.domain.valueobject.restaurant.DeliveryZone
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.GeoPoint
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import com.munchies.restaurant.domain.valueobject.restaurant.Table
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
  tables: List<Table>,
  deliveryZones: List<DeliveryZone>,
  val createdAt: LocalDateTime,
  var updatedAt: LocalDateTime,
) : AggregateRoot<RestaurantId>(id) {

  val name get(): RestaurantName = details.name
  val address get(): Address = details.address
  val phone get(): Phone = details.phone
  val email get(): Email = details.email

  /** The tables of the dining room, by table number; empty until the manager configures them. */
  var tables: List<Table> = tables.sortedBy { it.number }
    private set

  /**
   * Replaces the whole table layout.
   *
   * @throws IllegalArgumentException if two tables share the same number.
   */
  fun updateTables(newTables: List<Table>) {
    val duplicated = newTables.groupingBy { it.number }.eachCount().filterValues { it > 1 }.keys
    require(duplicated.isEmpty()) { "duplicated table numbers: ${duplicated.sorted()}" }
    this.tables = newTables.sortedBy { it.number }
    this.updatedAt = LocalDateTime.now()
  }

  /**
   * The delivery zones scheduler-service uses to decide which deliveries may share a driver, in
   * the order the manager listed them; empty until the manager draws them, in which case any two
   * deliveries may be batched as long as detour and trip budget allow it.
   */
  var deliveryZones: List<DeliveryZone> = deliveryZones
    private set

  /**
   * Replaces every delivery zone. Zones may overlap, but each must be identifiable on its own.
   *
   * @throws IllegalArgumentException if there are more than [MAX_DELIVERY_ZONES] zones, or two
   * zones share the same id or the same name (ignoring case).
   */
  fun updateDeliveryZones(newZones: List<DeliveryZone>) {
    require(newZones.size <= MAX_DELIVERY_ZONES) {
      "a restaurant cannot have more than $MAX_DELIVERY_ZONES delivery zones"
    }
    val duplicatedIds = newZones.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
    require(duplicatedIds.isEmpty()) { "duplicated zone ids: ${duplicatedIds.sorted()}" }
    val duplicatedNames = newZones.groupingBy { it.name.trim().lowercase() }.eachCount()
      .filterValues { it > 1 }.keys
    require(duplicatedNames.isEmpty()) { "duplicated zone names: ${duplicatedNames.sorted()}" }
    this.deliveryZones = newZones.toList()
    this.updatedAt = LocalDateTime.now()
  }

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
    /** Upper bound on the delivery zones of one restaurant. */
    const val MAX_DELIVERY_ZONES = 50

    fun create(managerId: UserId, details: RestaurantDetails): Restaurant {
      val now = LocalDateTime.now()
      return Restaurant(
        id = RestaurantId(),
        details = details,
        managerId = managerId,
        capacityPolicy = null,
        location = null,
        tables = emptyList(),
        deliveryZones = emptyList(),
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
      tables: List<Table> = emptyList(),
      deliveryZones: List<DeliveryZone> = emptyList(),
    ): Restaurant {
      return Restaurant(
        id = id,
        managerId = managerId,
        details = details,
        capacityPolicy = capacityPolicy,
        location = location,
        tables = tables,
        deliveryZones = deliveryZones,
        createdAt = createdAt,
        updatedAt = updatedAt,
      )
    }
  }
}
