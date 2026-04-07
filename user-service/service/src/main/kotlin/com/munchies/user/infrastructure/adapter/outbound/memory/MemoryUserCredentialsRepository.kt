package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserCredentialsRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["dev"])
interface MemoryUserCredentialsRepository : UserCredentialsRepository {
  val repository: InMemoryRepository<UserId, UserCredentials>

  override fun findById(id: UserId): UserCredentials? = repository.findById(id)
  override fun save(entity: UserCredentials) = repository.save(
    entity.copy(
      loginAttempts = 0,
      lastLogin = 0L,
      lockedUntil = -1L,
    ),
  )
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

  override fun delete(entity: UserCredentials) {
    repository.findById(entity.id)?.let { repository.delete(it) } ?: {}
  }
}
