package com.munchies.user.infrastructure.adapter.outbound.token

import com.munchies.commons.domain.port.*
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.model.exampleUser
import com.munchies.user.domain.model.update
import com.munchies.user.domain.port.TimeProvider
import com.munchies.user.domain.port.TokenRepository
import com.munchies.user.domain.port.UserRepository
import com.munchies.user.domain.port.defaultTimeProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class JsonWebTokenProviderTest {

  private fun getProvider(
    timeProvider: TimeProvider = mock(),
    userRepository: UserRepository = mock(),
    tokenRepository: TokenRepository = mock(),
    secret: String = "SECRET",
  ): TokenProvider = JsonWebTokenProvider(
    timeProvider = timeProvider,
    userRepository = userRepository,
    tokenRepository = tokenRepository,
    secret,
  )

  @Test
  fun `generate token returns failure when user is not found`() {
    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn null
      },
    )

    provider.generateToken(UserId()) shouldBe GenerateTokenFailure
  }

  @Test
  fun `generate token returns success when user is found`() {
    val userId = UserId()
    val user = exampleUser.update(userId)

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
    )

    val token = provider.generateToken(userId)

    (token as GenerateTokenSuccess).token.shouldNotBeEmpty()
  }

  @Test
  fun `validate token returns failure when token is revoked`() {
    val userId = UserId()
    val user = exampleUser.update(userId)

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn true
      },
    )
    val token = "token"

    provider.validateToken(token) shouldBe ValidateTokenFailure
  }

  @Test
  fun `validate token returns success when token has not expired`() {
    val userId = UserId()
    val user = exampleUser.update(userId)

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn false
      },
    )
    val token = (provider.generateToken(userId) as GenerateTokenSuccess).token

    val verifier = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn false
      },
    )

    verifier.validateToken(token) shouldBe ValidateTokenSuccess
  }

  @Test
  fun `validate token returns failure when token is invalid`() {
    val userId = UserId()
    val user = exampleUser.update(userId)

    val token = "prova"

    val verifier = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn false
      },
    )

    verifier.validateToken(token) shouldBe ValidateTokenFailure
  }

  @Test
  fun `refresh token returns failure when token is invalid`() {
    val userId = UserId()
    val user = exampleUser.update(userId)

    val token = "prova"

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn user
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn false
      },
    )

    provider.refreshToken(token) shouldBe RefreshTokenFailure
  }

  @Test
  fun `refresh token returns failure when user is invalid`() {
    val token = "prova"

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn null
      },
      mock<TokenRepository> {
        on { isRevoked(any()) } doReturn false
      },
    )

    provider.refreshToken(token) shouldBe RefreshTokenFailure
  }

  @Test
  fun `revoke token invokes repository`() {
    val tokenRepository = mock<TokenRepository> {
      on { isRevoked(any()) } doReturn false
      on { setRevoked("token") } doAnswer {}
    }

    val provider = getProvider(
      defaultTimeProvider(),
      mock<UserRepository> {
        on { findById(any()) } doReturn null
      },
      tokenRepository,
    )

    provider.revokeToken("token")
    verify(tokenRepository).setRevoked("token")
  }
}
