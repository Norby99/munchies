package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.RegisterUser.Companion.RegisterUserResult
import com.munchies.user.domain.factory.MockUserFactory
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class RegisterUserUseCaseTest {
  @Test
  fun `execute should return Success when user is not registered`() {
    val user = MockUserFactory().create("new-user-id")
    val credentials = UserCredentials(
      id = UserId("temporary-credentials-id"),
      passwordHash = "hashed-password",
      salt = "salt",
    )

    val userRepository = mock<UserRepository> {
      on { findById(any()) } doReturn null
    }
    val credentialsRepository = mock<UserCredentialsRepository>()
    val useCase = RegisterUserUseCase(userRepository, credentialsRepository)

    val result = useCase.execute(user, credentials)

    assertTrue(result is RegisterUserResult.Success)
    assertEquals(user, (result as RegisterUserResult.Success).user)
    verify(userRepository).save(user)

    val credentialsCaptor = argumentCaptor<UserCredentials>()
    verify(credentialsRepository).save(credentialsCaptor.capture())
    assertEquals(user.id, credentialsCaptor.firstValue.id)
    assertEquals(credentials.passwordHash, credentialsCaptor.firstValue.passwordHash)
    assertEquals(credentials.salt, credentialsCaptor.firstValue.salt)
  }

  @Test
  fun `execute should return UserIsAlreadyRegistered when user already exists`() {
    val user = MockUserFactory().create(
      "existing-user-id",
      profile = UserProfile.empty.copy(
        username = "existing-username",
        email = "existing-email",
      ),
    )
    val credentials = UserCredentials(
      id = UserId("irrelevant-id"),
      passwordHash = "hashed-password",
      salt = "salt",
    )

    val userRepository = mock<UserRepository> {
      on { findByEmail(any()) } doReturn user
      on { findByUsername(any()) } doReturn user
    }
    val credentialsRepository = mock<UserCredentialsRepository>()
    val useCase = RegisterUserUseCase(userRepository, credentialsRepository)

    val result = useCase.execute(user, credentials)
    val result2 = useCase.execute(user.copy(profile = user.profile.copy(email = "")), credentials)

    result shouldBeEqual result2
    assertTrue(result is RegisterUserResult.UserIsAlreadyRegistered)
    verify(userRepository, never()).save(any())
    verifyNoInteractions(credentialsRepository)
  }

  @Test
  fun `execute should return Failure when saving user throws Error`() {
    val user = MockUserFactory().create("user-id")
    val credentials = UserCredentials(
      id = UserId("credentials-id"),
      passwordHash = "hashed-password",
      salt = "salt",
    )

    val userRepository = mock<UserRepository> {
      on { findById(any()) } doReturn null
      on { save(any()) } doAnswer { throw IllegalAccessError("failed to save user") }
    }
    val credentialsRepository = mock<UserCredentialsRepository>()
    val useCase = RegisterUserUseCase(userRepository, credentialsRepository)

    val result = useCase.execute(user, credentials)

    assertTrue(result is RegisterUserResult.Failure)
    assertEquals(
      "failed to save user",
      (result as RegisterUserResult.Failure).reason,
    )
    verify(credentialsRepository, never()).save(any())
  }
}
