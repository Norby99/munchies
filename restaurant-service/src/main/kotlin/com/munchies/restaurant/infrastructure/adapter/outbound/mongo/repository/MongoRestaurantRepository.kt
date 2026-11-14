package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.repository

import com.munchies.restaurant.domain.aggregate.Restaurant
import com.munchies.restaurant.domain.repository.RestaurantRepository
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.UserId
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.RestaurantDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.RestaurantDocumentFactory.toDomain
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoFindQuery
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

@MongoRepository
sealed interface MongoCrudRestaurantRepository : CrudRepository<RestaurantDocument, String> {

  /**
   * Restaurants whose name or address matches [pattern], a regular expression applied
   * case-insensitively. Callers pass an escaped literal (see [MongoRestaurantRepository.search]).
   */
  @MongoFindQuery(
    "{ \$or: [ { name: { \$regex: :pattern, \$options: 'i' } }, " +
      "{ address: { \$regex: :pattern, \$options: 'i' } } ] }",
  )
  fun findByNameOrAddressMatching(pattern: String): List<RestaurantDocument>
}

@Singleton
@Requires(env = ["prod"])
class MongoRestaurantRepository(
  private val repository: MongoCrudRestaurantRepository,
) : RestaurantRepository {

  override fun findById(id: RestaurantId): Restaurant? =
    repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun save(entity: Restaurant) {
    repository.save(entity.toDocument())
  }

  override fun update(entity: Restaurant) {
    repository.update(entity.toDocument())
  }

  override fun delete(entity: Restaurant) {
    repository.delete(entity.toDocument())
  }

  override fun findAllByManagerId(managerId: UserId): List<Restaurant> =
    repository.findAll().filter { it.managerId == managerId.value }.map { it.toDomain() }

  // The filter runs in MongoDB. The text is escaped (\Q...\E) so that characters such as "." or
  // "(" typed by a customer are matched literally instead of being read as a pattern.
  override fun search(text: String): List<Restaurant> {
    val documents = if (text.isBlank()) {
      repository.findAll()
    } else {
      repository.findByNameOrAddressMatching(Regex.escape(text))
    }
    return documents.map { it.toDomain() }
  }
}
