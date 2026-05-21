package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserCredentialsDocument
import com.munchies.user.infrastructure.adapter.outbound.mongo.factory.UserCredentialsDocumentFactory
import io.kotest.matchers.shouldBe
import java.util.Optional
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MongoUserCredentialsRepositoryTest {

  private val credentialsId = "existing-credentials-id"

  @Test
  fun `findById should return mapped credentials when document exists`() {
    val document = UserCredentialsDocument(
      id = credentialsId,
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 3,
      lockedUntil = 1234L,
      lastLogin = 5678L,
    )
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { findById(credentialsId) } doReturn Optional.of(document)
    }

    val repository = MongoUserCredentialsRepository(crudRepository)

    val result = repository.findById(UserId(credentialsId))

    result shouldBe UserCredentials(
      id = UserId(credentialsId),
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 3,
      lockedUntil = 1234L,
      lastLogin = 5678L,
    )
    verify(crudRepository).findById(credentialsId)
  }

  @Test
  fun `findById should return null when document does not exist`() {
    val missingId = "missing-credentials-id"
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { findById(missingId) } doReturn Optional.empty()
    }

    val repository = MongoUserCredentialsRepository(crudRepository)

    val result = repository.findById(UserId(missingId))

    result shouldBe null
    verify(crudRepository).findById(missingId)
  }

  @Test
  fun `save should persist mapped credentials document`() {
    val credentialsDocument = UserCredentialsDocument(
      id = credentialsId,
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 0,
      lockedUntil = -1L,
      lastLogin = 0L,
    )
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { save(any()) } doReturn credentialsDocument
      on { findById(credentialsId) } doReturn Optional.of(
        credentialsDocument,
      )
    }
    val repository = MongoUserCredentialsRepository(crudRepository)
    val credentials = UserCredentials(
      id = UserId(credentialsId),
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 1,
      lockedUntil = 9999L,
      lastLogin = 8888L,
    )

    repository.save(credentials)

    repository.findById(UserId(credentialsId)) shouldBe credentials.copy(
      loginAttempts = 0,
      lastLogin = 0L,
      lockedUntil = -1L,
    )
  }

  @Test
  fun `update should persist mapped credentials document`() {
    val saltValue = "salt-value"
    val credentialsDocument = UserCredentialsDocument(
      id = credentialsId,
      passwordHash = "hashed-password",
      salt = saltValue,
      loginAttempts = 0,
      lockedUntil = -1L,
      lastLogin = 0L,
    )
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { update(any()) } doReturn credentialsDocument
      on { findById(credentialsId) } doReturnConsecutively listOf(
        Optional.of(
          credentialsDocument,
        ),
        Optional.of(
          credentialsDocument.copy(
            passwordHash = "new-hashed-password",
            loginAttempts = 2,
            lockedUntil = 7777L,
            lastLogin = 6666L,
          ),
        ),
      )
    }
    val repository = MongoUserCredentialsRepository(crudRepository)
    val credentials = UserCredentials(
      id = UserId(credentialsId),
      passwordHash = "new-hashed-password",
      salt = "",
      loginAttempts = 2,
      lockedUntil = 7777L,
      lastLogin = 6666L,
    )

    repository.update(credentials)

    repository.findById(UserId(credentialsId)) shouldBe UserCredentials(
      id = credentials.id,
      passwordHash = "new-hashed-password",
      salt = saltValue,
      loginAttempts = 2,
      lockedUntil = 7777L,
      lastLogin = 6666L,
    )
  }

  @Test
  fun `delete should remove mapped credentials document`() {
    val credentials = UserCredentials(
      id = UserId(credentialsId),
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 4,
      lockedUntil = 5555L,
      lastLogin = 4444L,
    )
    val documentFactory = UserCredentialsDocumentFactory.default
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { delete(any()) } doAnswer {}
      on { findById(credentialsId) } doReturnConsecutively listOf(
        Optional.of(documentFactory.run { credentials.toDocument() }),
        Optional.empty(),
      )
    }
    val repository = MongoUserCredentialsRepository(crudRepository)

    repository.delete(credentials)
    verify(crudRepository).delete(any())
    repository.findById(UserId(credentialsId)) shouldBe null
  }

  @Test
  fun `delete should not remove not existing credentials`() {
    val crudRepository = mock<MongoCrudUserCredentialsRepository> {
      on { findById(credentialsId) } doReturn Optional.empty()
    }
    val repository = MongoUserCredentialsRepository(crudRepository)
    val credentials = UserCredentials(
      id = UserId(credentialsId),
      passwordHash = "hashed-password",
      salt = "salt-value",
      loginAttempts = 4,
      lockedUntil = 5555L,
      lastLogin = 4444L,
    )

    repository.delete(credentials)

    repository.findById(UserId(credentialsId)) shouldBe null
  }
}
