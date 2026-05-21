package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

/**
 * Development-only in-memory implementation of [UserRepository].
 *
 * This adapter provides an in-memory storage solution for [User] entities, primarily for use in
 * development and testing environments. It avoids the need for a persistent database during local execution.
 *
 * Annotations:
 * - @Singleton: Marks this class as a singleton bean in the Micronaut application context.
 * - @Requires(env = ["dev"]): Ensures this implementation is only active in the "dev" environment.
 */
@Singleton
@Requires(env = ["dev"])
interface MemoryUserRepository : UserRepository {

  /**
   * Backing in-memory key-value repository used to store [User] entities by [UserId].
   *
   * This repository provides basic CRUD operations for managing user entities in memory.
   */
  val repository: InMemoryRepository<UserId, User>

  /**
   * Finds a user by its identifier.
   *
   * @param id The unique domain identifier of the user.
   * @return The matching [User] entity, or `null` if no entity is found with the given identifier.
   */
  override fun findById(id: UserId): User? = repository.findById(id)

  /**
   * Persists a new user entity in the in-memory repository.
   *
   * @param entity The [User] entity to persist.
   */
  override fun save(entity: User) = repository.save(entity)

  /**
   * Updates an existing user entity in the in-memory repository.
   *
   * @param entity The [User] entity with updated state to be saved.
   */
  override fun update(entity: User) = repository.update(entity)

  /**
   * Deletes an existing user entity from the in-memory repository.
   *
   * @param entity The [User] entity to remove.
   */
  override fun delete(entity: User) = repository.delete(entity)

  override fun findByEmail(email: String): User? =
    repository.findByPredicate { it.profile.email == email }

  override fun findByUsername(username: String): User? =
    repository.findByPredicate { it.profile.username == username }
}
