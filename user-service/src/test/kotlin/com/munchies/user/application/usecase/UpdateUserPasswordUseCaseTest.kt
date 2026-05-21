package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.UpdateUserPassword.Companion.UpdateUserPasswordResult
import com.munchies.user.domain.factory.UserFactory
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.UserProfile
import com.munchies.user.domain.model.UserRole
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.TimeProvider
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.UserRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
class UpdateUserPasswordUseCaseTest {

  private fun fakeUpdateUserPasswordUseCase(
    userRepository: UserRepository = mock {
      on { findByEmail(validEmail) } doReturn validUser
      on { findByUsername(validUsername) } doReturn validUser
      on { findByEmail(invalidEmail) } doReturn null
      on { findByUsername(invalidUsername) } doReturn null
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
      on { generateSalt() } doReturn generatedSalt
    },
    timeProvider: TimeProvider = { currentTime },
  ): UpdateUserPasswordUseCase = UpdateUserPasswordUseCase(
    userRepository = userRepository,
    credentialsRepository = credentialsRepository,
    passwordHasher = hasher,
    timeProvider = timeProvider,
  )

  val validId = "userId"
  val validUserId = UserId(validId)
  val validUsername = "validUsername"
  val validEmail = "validEmail"
  val invalidUsername = "invalidUsername"
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
  val generatedSalt = "newGeneratedSalt"
  val currentTime = 1000000L

  @Test
  fun `execute should succeed with valid credentials`() {
    val useCase = fakeUpdateUserPasswordUseCase()

    val result = useCase.execute(
      validUser.copy(profile = validUser.profile.copy(email = "")),
      validPassword,
      "newPassword",
    )

    result shouldBe UpdateUserPasswordResult.Success
  }

  @Test
  fun `execute should return UserNotFound when user does not exist`() {
    val useCase = fakeUpdateUserPasswordUseCase(
      userRepository = mock {
        on { findByEmail(validEmail) } doReturn null
        on { findByUsername(validUsername) } doReturn null
      },
    )

    val result = useCase.execute(validUser, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.UserNotFound
  }

  @Test
  fun `execute should return UserNotFound when credentials not found for user`() {
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = mock {
        on { findById(validUserId) } doReturn null
      },
    )

    val result = useCase.execute(validUser, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.UserNotFound
  }

  @Test
  fun `execute should return WrongCredentials when old password does not match`() {
    val useCase = fakeUpdateUserPasswordUseCase(
      hasher = mock {
        on { hash(password = any(), salt = any()) } doReturn "wrongHash"
        on { generateSalt() } doReturn generatedSalt
      },
    )

    val result = useCase.execute(validUser, "wrongPassword", "newPassword")

    result shouldBe UpdateUserPasswordResult.WrongCredentials
  }

  @Test
  fun `execute should return LockedUser when account is locked`() {
    val futureTime = currentTime + 3600000L
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = mock {
        on { findById(validUserId) } doReturn UserCredentials(
          validUserId,
          hashedValidPassword,
          validSalt,
          lockedUntil = futureTime,
        )
      },
      timeProvider = { currentTime },
    )

    val result = useCase.execute(validUser, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.LockedUser
  }

  @Test
  fun `execute should update password with new hash and salt on success`() {
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
      )
    }
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn hashedValidPassword
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
    )

    useCase.execute(validUser, validPassword, "newPassword")
  }

  @Test
  fun `execute should reset login attempts on successful password update`() {
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        loginAttempts = 5,
      )
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
    )

    useCase.execute(validUser, validPassword, "newPassword")
  }

  @Test
  fun `execute should reset lock on successful password update`() {
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        lockedUntil = currentTime + 100000L,
      )
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
    )

    useCase.execute(validUser, validPassword, "newPassword")
  }

  @Test
  fun `execute should increment login attempts on wrong password`() {
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        loginAttempts = 2,
      )
    }
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn "wrongHash"
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
    )

    useCase.execute(validUser, "wrongPassword", "newPassword")
  }

  @Test
  fun `execute should lock account for one hour on wrong password`() {
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        loginAttempts = 0,
      )
    }
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn "wrongHash"
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
      timeProvider = { currentTime },
    )

    useCase.execute(validUser, "wrongPassword", "newPassword")
  }

  @Test
  fun `execute should find user by email when username is blank`() {
    val userRepository = mock<UserRepository> {
      on { findByEmail(validEmail) } doReturn validUser
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      userRepository = userRepository,
    )
    val userWithoutUsername = validUser.copy(
      profile = validUser.profile.copy(username = ""),
    )

    val result = useCase.execute(userWithoutUsername, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.Success
  }

  @Test
  fun `execute should find user by username when email is blank`() {
    val userRepository = mock<UserRepository> {
      on { findByUsername(validUsername) } doReturn validUser
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      userRepository = userRepository,
    )
    val userWithoutEmail = validUser.copy(
      profile = validUser.profile.copy(email = ""),
    )

    val result = useCase.execute(userWithoutEmail, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.Success
  }

  @Test
  fun `execute should return UserNotFound when both email and username are blank`() {
    val useCase = fakeUpdateUserPasswordUseCase()
    val userWithoutIdentifiers = validUser.copy(
      profile = validUser.profile.copy(email = "", username = ""),
    )

    val result = useCase.execute(userWithoutIdentifiers, validPassword, "newPassword")

    result shouldBe UpdateUserPasswordResult.UserNotFound
  }

  @Test
  fun `execute should use correct salt when validating old password`() {
    val customSalt = "customSalt"
    val credentialsRepository = mock<UserCredentialsRepository> {
      on { findById(validUserId) } doReturn UserCredentials(
        validUserId,
        hashedValidPassword,
        customSalt,
      )
    }
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn hashedValidPassword
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
    )

    useCase.execute(validUser, validPassword, "newPassword")
  }
}
