package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.exampleUser
import com.munchies.user.domain.model.update
import com.munchies.user.infrastructure.adapter.outbound.mongo.factory.UserDocumentFactory.toDocument
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MongoUserRepositoryTest {

  val id = "existing-user-id"

  val userId = UserId(id)

  val realUserDoc =
    exampleUser.update(id = userId)
      .toDocument()

  @Test
  fun `mongo repository correctly finds existing user by id`() {
    val crudUserRepository = mock<MongoCrudUserRepository> {
      on { findById(id) } doReturn Optional.of(
        realUserDoc,
      )
    }

    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = mongoUserRepository.findById(userId)
    user shouldNotBe null
    user?.id?.shouldBeEqual(userId) ?: fail()
    verify(crudUserRepository).findById(id)
  }

  @Test
  fun `mongo repository correctly return null at non-existing user find`() {
    val crudUserRepository = mock<MongoCrudUserRepository> {
      on { findById("non-id") } doReturn Optional.empty()
    }

    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = mongoUserRepository.findById(UserId("non-$id"))
    user shouldBe null
    verify(crudUserRepository).findById("non-$id")
  }

  @Test
  fun save() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val userId = UserId("new-$id")
    val user = exampleUser.update(userId)

    mongoUserRepository.save(user)

    verify(crudUserRepository).save(any())
  }

  @Test
  fun update() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = exampleUser

    mongoUserRepository.update(user)

    verify(crudUserRepository).update(any())
  }

  @Test
  fun delete() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = exampleUser

    mongoUserRepository.delete(user)

    verify(crudUserRepository).delete(any())
  }
}
