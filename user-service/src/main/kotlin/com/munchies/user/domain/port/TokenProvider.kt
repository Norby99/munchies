package com.munchies.user.domain.port

import com.munchies.user.domain.model.UserId

interface TokenProvider {

  val idClaim: String
    get() = "id"
  val roleClaim: String
    get() = "role"
  val expirationClaim: String
    get() = "expiration"

  fun generateToken(userId: UserId): GenerateTokenResult
  fun validateToken(token: String): ValidateTokenResult
  fun refreshToken(expiredToken: String): RefreshTokenResult
  fun revokeToken(token: String)

  companion object {
    sealed interface GenerateTokenResult {
      data class Success(val token: String) : GenerateTokenResult
      data object Failure : GenerateTokenResult
    }
    sealed interface ValidateTokenResult {
      data object Success : ValidateTokenResult
      data object Failure : ValidateTokenResult
    }
    sealed interface RefreshTokenResult {
      data class Success(val token: String) : RefreshTokenResult
      data object Failure : RefreshTokenResult
    }
  }
}
