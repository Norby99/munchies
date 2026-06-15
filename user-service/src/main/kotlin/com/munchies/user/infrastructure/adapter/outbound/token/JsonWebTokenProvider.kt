package com.munchies.user.infrastructure.adapter.outbound.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.munchies.commons.UUIDEntityId
import com.munchies.commons.domain.port.*
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.*
import jakarta.inject.Singleton

@Singleton
class JsonWebTokenProvider(
  val timeProvider: TimeProvider = defaultTimeProvider(),
  val userRepository: UserRepository,
  val tokenRepository: TokenRepository,
  val jWTSecret: String = System.getenv(JWT_SECRET_ENV_NAME),
) : TokenProvider() {

  private val algorithm = Algorithm.HMAC256(jWTSecret)

  private val verifier = JWT.require(algorithm)
    .withClaimPresence(ID_CLAIM)
    .withClaimPresence(ROLE_CLAIM)
    .withClaimPresence(EXPIRATION_CLAIM)

  override fun generateToken(id: UUIDEntityId): GenerateTokenResult {
    return userRepository.findById(UserId(id.value))?.let { user ->
      GenerateTokenSuccess(
        JWT.create().withSubject(user.id.value)
          .withClaim(ID_CLAIM, user.id.value)
          .withClaim(ROLE_CLAIM, user.profile.role.name)
          .withClaim(EXPIRATION_CLAIM, timeProvider.addOneHour().invoke().toString())
          .withExpiresAt(timeProvider.addOneHour().toDate())
          .sign(algorithm),
      )
    }
      ?: GenerateTokenFailure.let {
        print("user not found")
        it
      }
  }

  override fun validateToken(token: String): ValidateTokenResult {
    if (tokenRepository.isRevoked(token)) return ValidateTokenFailure

    return try {
      verifier.build().verify(token)
      ValidateTokenSuccess
    } catch (_: JWTVerificationException) {
      ValidateTokenFailure
    }
  }

  override fun refreshToken(expiredToken: String): RefreshTokenResult {
    return try {
      val decoded = verifier
        .build()
        .verify(expiredToken)
      val userId = decoded.getClaim(ID_CLAIM).asString()
      generateToken(UserId(userId)).let { res ->
        if (res is GenerateTokenSuccess) {
          RefreshTokenSuccess(res.token)
        } else {
          RefreshTokenFailure
        }
      }
    } catch (_: JWTVerificationException) {
      RefreshTokenFailure
    }
  }

  override fun revokeToken(token: String) {
    tokenRepository.setRevoked(token)
  }
}
