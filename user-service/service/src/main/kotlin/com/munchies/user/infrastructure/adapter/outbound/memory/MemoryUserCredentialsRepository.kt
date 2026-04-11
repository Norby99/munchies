package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserCredentialsRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

/**
 * In-memory implementation of the UserCredentialsRepository interface.
 *
 * This repository is used for managing UserCredentials entities in a development environment.
 * It relies on an in-memory data store provided by the InMemoryRepository class.
 *
 * Annotations:
 * - @Singleton: Marks this class as a singleton bean in the Micronaut application context.
 * - @Requires(env = ["dev"]): Ensures this implementation is only active in the "dev" environment.
 */
@Singleton
@Requires(env = ["dev"])
interface MemoryUserCredentialsRepository : UserCredentialsRepository {

  /**
   * The in-memory repository instance used for storing and managing UserCredentials entities.
   */
  val repository: InMemoryRepository<UserId, UserCredentials>

  /**
   * Finds a UserCredentials entity by its unique identifier.
   *
   * @param id The unique identifier of the UserCredentials entity.
   * @return The UserCredentials entity if found, or null if not found.
   */
  override fun findById(id: UserId): UserCredentials? = repository.findById(id)

  /**
   * Saves a new UserCredentials entity to the repository.
   *
   * This method resets the loginAttempts, lastLogin, and lockedUntil fields to their default values.
   *
   * @param entity The UserCredentials entity to be saved.
   */
  override fun save(entity: UserCredentials) = repository.save(
    entity.copy(
      loginAttempts = 0,
      lastLogin = 0L,
      lockedUntil = -1L,
    ),
  )

  /**
   * Updates an existing UserCredentials entity in the repository.
   *
   * This method updates the fields of the existing entity with the values from the provided entity.
   * If certain fields (passwordHash, salt) are empty in the provided entity, the existing values are retained.
   *
   * @param entity The UserCredentials entity with updated values.
   */
  override fun update(entity: UserCredentials) {
    repository.findById(entity.id)?.let {
      repository.update(
        it.copy(
          passwordHash = entity.passwordHash.ifEmpty { it.passwordHash },
          salt = entity.salt.ifEmpty { it.salt },
          loginAttempts = entity.loginAttempts,
          lockedUntil = entity.lockedUntil,
          lastLogin = entity.lastLogin,
        ),
      )
    } ?: {}
  }

  /**
   * Deletes a UserCredentials entity from the repository.
   *
   * If the entity exists in the repository, it is removed. Otherwise, no action is taken.
   *
   * @param entity The UserCredentials entity to be deleted.
   */
  override fun delete(entity: UserCredentials) {
    repository.findById(entity.id)?.let { repository.delete(it) } ?: {}
  }
}
