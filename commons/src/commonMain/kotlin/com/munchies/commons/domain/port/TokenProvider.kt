package com.munchies.commons.domain.port

import com.munchies.commons.UUIDEntityId
import kotlin.js.JsExport

@JsExport
val JWT_SECRET_ENV_NAME = "JWT_SECRET"

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

@JsExport sealed interface GenerateTokenResult

@JsExport data class GenerateTokenSuccess(val token: String) : GenerateTokenResult

@JsExport data object GenerateTokenFailure : GenerateTokenResult

@JsExport sealed interface ValidateTokenResult

@JsExport data object ValidateTokenSuccess : ValidateTokenResult

@JsExport data object ValidateTokenFailure : ValidateTokenResult

@JsExport sealed interface RefreshTokenResult

@JsExport data class RefreshTokenSuccess(val token: String) : RefreshTokenResult

@JsExport data object RefreshTokenFailure : RefreshTokenResult
