package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.shouldBe
import kotlin.invoke
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LoginUserUseCaseTest {

  private fun fakeLoginUserUserUseCase(
    userRepository: UserRepository = mock {
      on { findById(validUserId) } doReturn validUser
      on { findByUsername(validUsername) } doReturn validUser
      on { findByEmail(validEmail) } doReturn validUser
      on { findByUsername(invalidUsername) } doReturn null
      on { findByEmail(invalidEmail) } doReturn null
    },
    credentialsRepository: UserCredentialsRepository = mock {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
      )
    },
    hasher: PasswordHasher = mock {
      on { hash(password = any(), salt = any()) } doReturn hashedValidPassword
    },
  ): LoginUserUseCase = LoginUserUseCase(
    userRepository = userRepository,
    credentialsRepository = credentialsRepository,
    passwordHasher = hasher,
  )

  val validId = "userId"
  val invalidId = "invalidUserId"
  val validUserId = UserId(validId)
  val invalidUserId = UserId(invalidId)
  val validUsername = "validUsername"
  val invalidUsername = "invalidUsername"
  val validEmail = "validEmail"
  val invalidEmail = "invalidEmail"
  val validUser = UserFactory.default.create(
    validId,
    UserProfile.empty.copy(
      username = validUsername,
      email = validEmail,
      role = UserRole.MANAGER,
    ),
  )

  val validPassword = "validPassword"
  val validSalt = "randomSalt"
  val hashedValidPassword = "hashedValidPassword"

  @Test
  fun `login should succeed with valid credentials`() {
    val useCase = fakeLoginUserUserUseCase()

    val result = useCase.execute(validEmail, "", validPassword)
    result shouldBe LoginResult.Success(validId)

    val result2 = useCase.execute("", validUsername, validPassword)
    result2 shouldBe LoginResult.Success(validId)
  }

  @Test
  fun `login should fail with invalid credentials`() {
    val useCase = fakeLoginUserUserUseCase()
    val result = useCase.execute("", invalidUsername, "invalidPassword")
    result shouldBe LoginResult.Failure

    val result2 = useCase.execute(invalidEmail, "", "invalidPassword")
    result2 shouldBe LoginResult.Failure
  }

  @Test
  fun `login should fail when username and email are empty`() {
    val useCase = fakeLoginUserUserUseCase()
    val result = useCase.execute("", "", validPassword)
    result shouldBe LoginResult.Failure
  }

  @Test
  fun `login should fail when id is not found`() {
    val useCase = fakeLoginUserUserUseCase(
      credentialsRepository = mock {
        on { findById(any()) } doReturn null
      },
    )
    val result = useCase.execute(validEmail, "", validPassword)
    result shouldBe LoginResult.Failure
  }

  @Test
  fun `login should fail when username and email are not found`() {
    val useCase = fakeLoginUserUserUseCase()
    val result = useCase.execute("", "", validPassword)
    result shouldBe LoginResult.Failure
  }

  @Test
  fun `login should fail when user is locked`() {
    // TODO
  }
}
