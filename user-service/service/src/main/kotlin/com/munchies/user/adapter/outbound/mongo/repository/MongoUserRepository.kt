package com.munchies.user.adapter.outbound.mongo.repository

import com.munchies.user.adapter.outbound.mongo.document.UserDocument
import com.munchies.user.adapter.outbound.mongo.mapper.DomainToDocument.toDocument
import com.munchies.user.adapter.outbound.mongo.mapper.DomainToDocument.toDomain
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository
import jakarta.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@MongoRepository
interface MongoCrudUserRepository : CrudRepository<UserDocument, String>

@Singleton
@Requires(env = ["prod"])
class MongoUserRepository(
  private val repository: MongoCrudUserRepository,
) : UserRepository {

  override fun findById(id: UserId): User? = repository.findById(id.value).map {
    it.toDomain()
  }.getOrNull()

  override fun save(entity: User) {
    repository.save(entity.toDocument())
  }

  override fun update(entity: User) {
    repository.update(entity.toDocument())
  }

  override fun delete(entity: User) {
    repository.delete(entity.toDocument())
  }
}
