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
 * This adapter is registered as a Micronaut bean only when the `dev` environment is active,
 * allowing local execution and tests without a persistent database.
 */
@Singleton
@Requires(env = ["dev"])
interface MemoryUserRepository : UserRepository {

  /**
   * Backing in-memory key-value repository used to store [User] entities by [UserId].
   */
  val repository: InMemoryRepository<UserId, User>

  /**
   * Finds a user by its identifier.
   *
   * @param id unique domain identifier of the user.
   * @return the matching [User], or `null` when no entity is found.
   */
  override fun findById(id: UserId): User? = repository.findById(id)

  /**
   * Persists a new user entity.
   *
   * @param entity user entity to persist.
   */
  override fun save(entity: User) = repository.save(entity)

  /**
   * Updates an existing user entity.
   *
   * @param entity user entity with updated state.
   */
  override fun update(entity: User) = repository.update(entity)

  /**
   * Deletes an existing user entity.
   *
   * @param entity user entity to remove.
   */
  override fun delete(entity: User) = repository.delete(entity)
}
