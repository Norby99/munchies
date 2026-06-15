package com.munchies.commons.domain.port

import com.munchies.commons.UUIDEntityId
import kotlin.js.JsExport

@JsExport
abstract class TokenProvider {
  abstract fun generateToken(id: UUIDEntityId): GenerateTokenResult
  abstract fun validateToken(token: String): ValidateTokenResult
  abstract fun refreshToken(expiredToken: String): RefreshTokenResult
  abstract fun revokeToken(token: String)
}
@JsExport
val ID_CLAIM: String = "id"
@JsExport
val ROLE_CLAIM: String = "role"
@JsExport
val EXPIRATION_CLAIM: String = "expiration"

@JsExport
sealed interface GenerateTokenResult {
  data class Success(val token: String) : GenerateTokenResult
  data object Failure : GenerateTokenResult
}

@JsExport
sealed interface ValidateTokenResult {
  data object Success : ValidateTokenResult
  data object Failure : ValidateTokenResult
}

@JsExport
sealed interface RefreshTokenResult {
  data class Success(val token: String) : RefreshTokenResult
  data object Failure : RefreshTokenResult
}
