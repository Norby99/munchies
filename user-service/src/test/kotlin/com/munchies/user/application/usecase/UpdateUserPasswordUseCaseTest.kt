package com.munchies.user.application.usecase

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.application.port.inbound.UpdateUserPassword.Companion.UpdateUserPasswordResult
import com.munchies.user.domain.model.*
import com.munchies.user.domain.port.*
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserCredentialsRepository
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

  private class MemoryUserCredentialRepositoryImpl(
    override val repository: InMemoryRepository<UserId, UserCredentials> = InMemoryRepository(),
  ) : MemoryUserCredentialsRepository

  val validId = "userId"
  val validUserId = UserId(validId)
  val validUsername = "validUsername"
  val validEmail = "validEmail"
  val invalidUsername = "invalidUsername"
  val invalidEmail = "invalidEmail"
  val validUser = exampleUser.update(
    validId,
    UserProfile(
      username = validUsername,
      email = Email(validEmail),
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
      validId,
      validUsername,
      validEmail,
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

    val result = useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )
    result shouldBe UpdateUserPasswordResult.UserNotFound
  }

  @Test
  fun `execute should return UserNotFound when credentials not found for user`() {
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = mock {
        on { findById(validUserId) } doReturn null
      },
    )

    val result = useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )
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

    val result = useCase.execute(
      validId,
      validUsername,
      validEmail,
      "wrongPassword",
      "newPassword",
    )
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

    val result = useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )
    result shouldBe UpdateUserPasswordResult.LockedUser
  }

  @Test
  fun `execute should update password with new hash and salt on success`() {
    val repo = MemoryUserCredentialRepositoryImpl()

    repo.save(
      UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
      ),
    )

    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn hashedValidPassword
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = repo,
      hasher = hasher,
    )

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )

    val updated = repo.findById(validUserId)
    updated shouldNotBe null
    updated!!.passwordHash shouldBe hashedValidPassword
  }

  @Test
  fun `execute should reset login attempts on successful password update`() {
    val credentialsRepository = MemoryUserCredentialRepositoryImpl()
    credentialsRepository.save(
      UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        loginAttempts = 5,
      ),
    )
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
    )

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )

    val updated = credentialsRepository.findById(validUserId)
    updated shouldNotBe null
    updated!!.loginAttempts shouldBe 0
  }

  @Test
  fun `execute should reset lock on successful password update`() {
    val lockedUntil = currentTime - 1000L
    val credentialsRepository = MemoryUserCredentialRepositoryImpl()
    credentialsRepository.save(
      UserCredentials(
        validUserId,
        hashedValidPassword,
        validSalt,
        lockedUntil = currentTime,
      ),
    )

    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
    )

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )

    val updated = credentialsRepository.findById(validUserId)
    updated shouldNotBe null
    updated!!.lockedUntil shouldNotBe lockedUntil
  }

  @Test
  fun `execute should increment login attempts on wrong password`() {
    val attemps = 2
    val credentialsRepository = MemoryUserCredentialRepositoryImpl()
    val cred = UserCredentials(
      id = validUserId,
      passwordHash = hashedValidPassword,
      salt = validSalt,
      lockedUntil = 0,
      lastLogin = 0,
      loginAttempts = attemps,
    )
    credentialsRepository.save(cred)
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn "wrongHash"
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
    )

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      "wrongPassword",
      "newPassword",
    )

    val updated = credentialsRepository.findById(validUserId)
    updated shouldNotBe null
    println(updated)
    updated!!.loginAttempts shouldBe attemps + 1
  }

  @Test
  fun `execute should lock account for one hour on wrong password`() {
    val time = defaultTimeProvider().invoke()

    val cred = UserCredentials(
      validUserId,
      hashedValidPassword,
      validSalt,
      loginAttempts = 0,
      lockedUntil = 0,
    )
    val credentialsRepository = MemoryUserCredentialRepositoryImpl()
    credentialsRepository.save(cred)
    val hasher = mock<PasswordHasher> {
      on { hash(password = any(), salt = any()) } doReturn "wrongHash"
      on { generateSalt() } doReturn generatedSalt
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      credentialsRepository = credentialsRepository,
      hasher = hasher,
      timeProvider = { time },
    )

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      "wrongPassword",
      "newPassword",
    ).shouldBeInstanceOf<UpdateUserPasswordResult.WrongCredentials>()

    val updated = credentialsRepository.findById(validUserId)
    updated shouldNotBe null
    updated!!.loginAttempts shouldBe 1
    updated.lockedUntil shouldBeGreaterThan time
  }

  @Test
  fun `execute should find user by email when username is blank`() {
    val userRepository = mock<UserRepository> {
      on { findByEmail(validEmail) } doReturn validUser
    }
    val useCase = fakeUpdateUserPasswordUseCase(
      userRepository = userRepository,
    )
    val result = useCase.execute(
      validId,
      "",
      validEmail,
      validPassword,
      "newPassword",
    )
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

    val result = useCase.execute(
      validId,
      validUsername,
      "",
      validPassword,
      "newPassword",
    )
    result shouldBe UpdateUserPasswordResult.Success
  }

  @Test
  fun `execute should return UserNotFound when both email and username are blank`() {
    val useCase = fakeUpdateUserPasswordUseCase()
    val result = useCase.execute(
      validId,
      "",
      "",
      validPassword,
      "newPassword",
    )
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

    useCase.execute(
      validId,
      validUsername,
      validEmail,
      validPassword,
      "newPassword",
    )
  }
}
