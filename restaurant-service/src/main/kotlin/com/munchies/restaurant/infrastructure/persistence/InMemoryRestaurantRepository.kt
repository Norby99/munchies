package com.munchies.restaurant.infrastructure.persistence

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.aggregate.RestaurantDetails
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.domain.valueobject.restaurant.Address
import com.munchies.restaurant.domain.valueobject.restaurant.Email
import com.munchies.restaurant.domain.valueobject.restaurant.Phone
import com.munchies.restaurant.domain.valueobject.restaurant.RestaurantName
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of Restaurant Repository
 * Used for testing and as placeholder for real persistence
 */
class InMemoryRestaurantRepository : RestaurantRepository {
  private val restaurants = ConcurrentHashMap<String, RestaurantEntity>()

  override fun save(entity: Restaurant) {
    val restaurantEntity = RestaurantEntity(
      id = entity.id.value,
      managerId = entity.managerId.value,
      name = entity.details.name.value,
      address = entity.details.address.value,
      phone = entity.details.phone.value,
      email = entity.details.email.value,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
    )
    restaurants[entity.id.value] = restaurantEntity
  }

  override fun update(entity: Restaurant) {
    val restaurantEntity = RestaurantEntity(
      id = entity.id.value,
      name = entity.details.name.value,
      address = entity.details.address.value,
      phone = entity.details.phone.value,
      email = entity.details.email.value,
      managerId = entity.managerId.value,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
    )
    restaurants[entity.id.value] = restaurantEntity
  }

  override fun delete(entity: Restaurant) {
    restaurants.remove(entity.id.value)
  }

  override fun create(): RestaurantId {
    return RestaurantId()
  }

  override fun findById(id: RestaurantId): Restaurant? {
    val entity = restaurants[id.value] ?: return null
    return entity.toDomain()
  }

  override suspend fun findByIdSuspend(id: RestaurantId): Restaurant? {
    val entity = restaurants[id.value] ?: return null
    return entity.toDomain()
  }

  override suspend fun findByManagerId(managerId: UserId): List<Restaurant> {
    return restaurants.values
      .filter { it.managerId == managerId.value }
      .map { it.toDomain() }
  }

  override suspend fun deleteById(id: RestaurantId): Boolean {
    return restaurants.remove(id.value) != null
  }

  override suspend fun existsById(id: RestaurantId): Boolean {
    return restaurants.containsKey(id.value)
  }

  /**
   * Internal entity for restaurant representation in repository
   */
  private data class RestaurantEntity(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val managerId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
  ) {
    fun toDomain(): Restaurant {
      return Restaurant.fromDatabase(
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
  }
}
