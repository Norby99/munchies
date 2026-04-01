package com.munchies.user.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoCrudUserRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoUserRepository
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.Optional
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MongoUserRepositoryTest {

  @Test
  fun `mongo repository correctly finds existing user by id`() {
    val crudUserRepository = mock<MongoCrudUserRepository> {
      on { findById("existing-user-id") } doReturn Optional.of(
        UserDocument("existing-user-id"),
      )
    }

    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = mongoUserRepository.findById(UserId("existing-user-id"))
    user shouldNotBe null
    user?.id?.shouldBeEqual(UserId("existing-user-id"))
    verify(crudUserRepository).findById("existing-user-id")
  }

  @Test
  fun `mongo repository correctly return null at non-existing user find`() {
    val crudUserRepository = mock<MongoCrudUserRepository> {
      on { findById("non-existing-user-id") } doReturn Optional.empty()
    }

    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val user = mongoUserRepository.findById(UserId("non-existing-user-id"))
    user shouldBe null
    verify(crudUserRepository).findById("non-existing-user-id")
  }

  @Test
  fun save() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val userId = UserId("new-user-id")
    val user = User(userId)

    mongoUserRepository.save(user)

    verify(crudUserRepository).save(UserDocument("new-user-id"))
  }

  @Test
  fun update() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val userId = UserId("existing-user-id")
    val user = User(userId)

    mongoUserRepository.update(user)

    verify(crudUserRepository).update(UserDocument("existing-user-id"))
  }

  @Test
  fun delete() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val userId = UserId("existing-user-id")
    val user = User(userId)

    mongoUserRepository.delete(user)

    verify(crudUserRepository).delete(UserDocument("existing-user-id"))
  }

  @Test
  fun create() {
    val crudUserRepository = mock<MongoCrudUserRepository>()
    val mongoUserRepository = MongoUserRepository(crudUserRepository)

    val newUserId = mongoUserRepository.create()

    newUserId.value shouldNotBe null
    verify(crudUserRepository).save(UserDocument(newUserId.value))
  }
}
