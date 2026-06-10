package com.munchies.user.infrastructure.adapter.outbound.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.*
import com.munchies.user.domain.port.TokenProvider.Companion.GenerateTokenResult
import com.munchies.user.domain.port.TokenProvider.Companion.RefreshTokenResult
import com.munchies.user.domain.port.TokenProvider.Companion.ValidateTokenResult

class JsonWebTokenProvider(
  val timeProvider: TimeProvider,
  val userRepository: UserRepository,
  val tokenRepository: TokenRepository,
  val jWTSecret: String,
) : TokenProvider {

  private val algorithm = Algorithm.HMAC256(jWTSecret)

  private val verifier = JWT.require(algorithm)
    .withClaimPresence(idClaim)
    .withClaimPresence(roleClaim)
    .withClaimPresence(expirationClaim)

  override fun generateToken(userId: UserId): GenerateTokenResult {
    return userRepository.findById(userId)?.let { user ->
      GenerateTokenResult.Success(
        JWT.create().withSubject(user.id.value)
          .withClaim(idClaim, user.id.value)
          .withClaim(roleClaim, user.profile.role.name)
          .withClaim(expirationClaim, timeProvider.addOneHour().invoke().toString())
          .withExpiresAt(timeProvider.addOneHour().toDate())
          .sign(algorithm),
      )
    }
      ?: GenerateTokenResult.Failure.let {
        print("user not found")
        it
      }
  }

  override fun validateToken(token: String): ValidateTokenResult {
    if (tokenRepository.isRevoked(token)) return ValidateTokenResult.Failure

    return try {
      verifier.build().verify(token)
      ValidateTokenResult.Success
    } catch (_: JWTVerificationException) {
      ValidateTokenResult.Failure
    }
  }

  override fun refreshToken(expiredToken: String): RefreshTokenResult {
    return try {
      val decoded = verifier
        .build()
        .verify(expiredToken)
      val userId = decoded.getClaim(idClaim).asString()
      generateToken(UserId(userId)).let { res ->
        if (res is GenerateTokenResult.Success) {
          RefreshTokenResult.Success(res.token)
        } else {
          RefreshTokenResult.Failure
        }
      }
    } catch (_: JWTVerificationException) {
      RefreshTokenResult.Failure
    }
  }

  override fun revokeToken(token: String) {
    tokenRepository.setRevoked(token)
  }
}
