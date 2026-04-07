package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument
import com.munchies.user.infrastructure.adapter.outbound.mongo.factory.UserDocumentFactory
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton
import kotlin.jvm.optionals.getOrNull

/**
 * Micronaut Data Mongo repository contract for low-level CRUD operations on [UserDocument].
 *
 * This interface is used by [MongoUserRepository] as the infrastructure persistence gateway.
 */
@MongoRepository
sealed interface MongoCrudUserRepository : CrudRepository<UserDocument, String>

/**
 * Production-only MongoDB-backed implementation of the domain [UserRepository].
 *
 * This adapter maps domain entities to Mongo documents and delegates persistence
 * to [MongoCrudUserRepository].
 *
 * @property repository low-level CRUD repository for [UserDocument] entities.
 */
@Singleton
@Requires(env = ["prod"])
class MongoUserRepository(
  private val repository: MongoCrudUserRepository,
  private val documentFactory: UserDocumentFactory = UserDocumentFactory.default,
) : UserRepository {

  /**
   * Retrieves a [User] by its domain identifier.
   *
   * @param id domain identifier of the user.
   * @return the mapped domain [User] when found, or `null` otherwise.
   */
  override fun findById(id: UserId): User? = repository.findById(id.value).map {
    documentFactory.run { it.toDomain() }
  }.getOrNull()

  /**
   * Persists a new [User] entity.
   *
   * @param entity domain user to be stored.
   */
  override fun save(entity: User) {
    repository.save(documentFactory.run { entity.toDocument() })
  }

  /**
   * Updates an existing [User] entity.
   *
   * @param entity domain user with updated state.
   */
  override fun update(entity: User) {
    repository.update(documentFactory.run { entity.toDocument() })
  }

  /**
   * Deletes an existing [User] entity.
   *
   * @param entity domain user to remove.
   */
  override fun delete(entity: User) {
    repository.delete(documentFactory.run { entity.toDocument() })
  }
}
