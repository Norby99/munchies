package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserCredentialsDocument
import com.munchies.user.infrastructure.adapter.outbound.mongo.factory.UserCredentialsDocumentFactory
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton

/**
 * MongoDB repository for managing UserCredentials entities.
 *
 * This repository provides an implementation of the UserCredentialsRepository interface
 * for interacting with MongoDB. It uses Micronaut Data's CrudRepository for basic CRUD operations
 * and a factory for converting between domain models and MongoDB documents.
 */

/**
 * MongoDB CRUD repository for UserCredentialsDocument.
 *
 * This interface extends Micronaut Data's CrudRepository to provide basic CRUD operations
 * for UserCredentialsDocument entities in MongoDB.
 */
@MongoRepository
sealed interface MongoCrudUserCredentialsRepository :
  CrudRepository<UserCredentialsDocument, String>

/**
 * Production implementation of the UserCredentialsRepository interface using MongoDB.
 *
 * This class provides methods for managing UserCredentials entities in a MongoDB database.
 * It uses the MongoCrudUserCredentialsRepository for database operations and the
 * UserCredentialsDocumentFactory for converting between domain models and MongoDB documents.
 *
 * Annotations:
 * - @Singleton: Marks this class as a singleton bean in the Micronaut application context.
 * - @Requires(env = ["prod"]): Ensures this implementation is only active in the "prod" environment.
 *
 * @property repository The MongoDB CRUD repository for UserCredentialsDocument.
 * @property documentFactory The factory for converting between UserCredentials and UserCredentialsDocument.
 */
@Singleton
@Requires(env = ["prod"])
class MongoUserCredentialsRepository(
  private val repository: MongoCrudUserCredentialsRepository,
  private val documentFactory: UserCredentialsDocumentFactory =
    UserCredentialsDocumentFactory.default,
) : UserCredentialsRepository {

  /**
   * Finds a UserCredentials entity by its unique identifier.
   *
   * @param id The unique identifier of the UserCredentials entity.
   * @return The UserCredentials entity if found, or null if not found.
   */
  override fun findById(id: UserId): UserCredentials? = repository.findById(id.value).map {
    documentFactory.run { it.toDomain() }
  }.orElse(null)

  override fun findByPredicate(predicate: (UserCredentials) -> Boolean): UserCredentials? =
    repository.findAll().asSequence()
      .map { documentFactory.run { it.toDomain() } }
      .firstOrNull(predicate)

  /**
   * Saves a new UserCredentials entity to the MongoDB database.
   *
   * This method resets the loginAttempts, lastLogin, and lockedUntil fields to their default values
   * before saving the entity.
   *
   * @param entity The UserCredentials entity to be saved.
   */
  override fun save(entity: UserCredentials) {
    repository.save(
      documentFactory.run {
        entity.toDocument()
          .copy(
            loginAttempts = 0,
            lastLogin = 0L,
            lockedUntil = -1L,
          )
      },
    )
  }

  /**
   * Updates an existing UserCredentials entity in the MongoDB database.
   *
   * This method updates the fields of the existing entity with the values from the provided entity.
   * If certain fields (passwordHash, salt) are empty in the provided entity, the existing values are retained.
   *
   * @param entity The UserCredentials entity with updated values.
   */
  override fun update(entity: UserCredentials) {
    this.findById(entity.id)?.let {
      repository.update(
        documentFactory.run {
          documentFactory.run {
            it.copy(
              passwordHash = entity.passwordHash.ifEmpty { it.passwordHash },
              salt = entity.salt.ifEmpty { it.salt },
              loginAttempts = entity.loginAttempts,
              lockedUntil = entity.lockedUntil,
              lastLogin = entity.lastLogin,
            ).toDocument()
          }
        },
      )
    }
  }

  /**
   * Deletes a UserCredentials entity from the MongoDB database.
   *
   * If the entity exists in the database, it is removed. Otherwise, no action is taken.
   *
   * @param entity The UserCredentials entity to be deleted.
   */
  override fun delete(entity: UserCredentials) {
    this.findById(entity.id)
      ?.let {
        repository.delete(documentFactory.run { entity.toDocument() })
      } ?: {}
  }
}
