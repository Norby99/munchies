package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.VerifyUserEmail.Companion.VerifyUserEmailResult.ConfirmedEmail
import com.munchies.user.application.port.inbound.VerifyUserEmail.Companion.VerifyUserEmailResult.InvalidRequest
import com.munchies.user.domain.model.Email
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class VerifyUserEmailUseCaseTest {
  val userId = UserId()
  val fakeId = UserId(userId.value + "fake")
  val email = "email"
  val user =
    User(id = userId, profile = UserProfile.empty.copy(email = Email(email)))

  val otk = "otk"

  val userRepository = mock<UserRepository> {
    on { findById(userId) } doReturn user
    on { findById(fakeId) } doReturn null
  }

  val hasher: PasswordHasher = mock<PasswordHasher> {
    on { hash(userId.value, email) } doReturn otk
    on { hash(fakeId.value, email) } doReturn otk + "fake"
  }

  @Test
  fun `execute should return invalid request when user doesnt exist`() {
    val useCase = VerifyUserEmailUseCase(userRepository, hasher)
    useCase.execute(fakeId.value, otk) shouldBe InvalidRequest
    verify(userRepository).findById(fakeId)
    verifyNoInteractions(hasher)
  }

  @Test
  fun `execute should return invalid request when otk doesnt match`() {
    val useCase = VerifyUserEmailUseCase(userRepository, hasher)
    useCase.execute(userId.value, otk + "fake") shouldBe InvalidRequest
    verify(userRepository).findById(userId)
    verify(hasher).hash(userId.value, email)
  }

  @Test
  fun `execute should return confirmed email when otk matches`() {
    val useCase = VerifyUserEmailUseCase(userRepository, hasher)
    useCase.execute(userId.value, otk) shouldBe ConfirmedEmail
    verify(userRepository).findById(userId)
    verify(hasher).hash(userId.value, email)
  }
}
