package com.munchies.user.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["dev"])
interface MemoryUserRepository : UserRepository {

  val repository: InMemoryRepository<UserId, User>

  override fun findById(id: UserId): User? = repository.findById(id)

  override fun save(entity: User) = repository.save(entity)

  override fun update(entity: User) = repository.update(entity)

  override fun delete(entity: User) = repository.delete(entity)

  override fun create(): UserId {
    val userId = UserId()
    repository.save(User(userId))
    return userId
  }
}
