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

@MongoRepository
sealed interface MongoCrudUserCredentialsRepository :
  CrudRepository<UserCredentialsDocument, String>

@Singleton
@Requires(env = ["prod"])
class MongoUserCredentialsRepository(
  private val repository: MongoCrudUserCredentialsRepository,
  private val documentFactory: UserCredentialsDocumentFactory =
    UserCredentialsDocumentFactory.default,
) : UserCredentialsRepository {

  override fun findById(id: UserId): UserCredentials? = repository.findById(id.value).map {
    documentFactory.run { it.toDomain() }
  }.orElse(null)

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

  override fun delete(entity: UserCredentials) {
    this.findById(entity.id)
      ?.let {
        repository.delete(documentFactory.run { entity.toDocument() })
      } ?: {}
  }
}
