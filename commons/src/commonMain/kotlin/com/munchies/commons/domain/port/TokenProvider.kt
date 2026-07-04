package com.munchies.commons.domain.port

import com.munchies.commons.UUIDEntityId
import kotlin.js.JsExport

@JsExport
val JWT_SECRET_ENV_NAME = "JWT_SECRET"

@JsExport
val JWT_SECRET_ALGORITHM = "HMAC256"

@JsExport
abstract class TokenProvider {
  abstract fun generateToken(id: UUIDEntityId, role: AuthRole): GenerateTokenResult
  abstract fun validateToken(token: String): ValidateTokenResult
  abstract fun refreshToken(expiredToken: String): RefreshTokenResult
  abstract fun revokeToken(token: String)
}

@JsExport
abstract class TokenDecoder {
  abstract fun validateAndDecodeToken(token: String): DecodedTokenResult
}

@JsExport
enum class AuthRole {
  CUSTOMER(1),
  MANAGER(2),
  ;

  constructor(
    visibility: Int,
  ) {
    this.visibility = visibility
  }
  val visibility: Int
}

@JsExport
fun isAuthRoleGreaterThan(role: AuthRole, other: AuthRole): Boolean =
  role.visibility >= other.visibility

@JsExport abstract class DecodedTokenResult

@JsExport data class DecodedTokenSuccess(
  val id: String,
  val role: AuthRole,
) : DecodedTokenResult()

@JsExport data class DecodedTokenFailure(val reason: String) : DecodedTokenResult()

@JsExport
val ID_CLAIM: String = "id"

@JsExport
val ROLE_CLAIM: String = "role"

@JsExport
val EXPIRATION_CLAIM: String = "expiration"

@JsExport sealed interface GenerateTokenResult

@JsExport data class GenerateTokenSuccess(val token: String) : GenerateTokenResult

@JsExport data class GenerateTokenFailure(val reason: String) : GenerateTokenResult

@JsExport sealed interface ValidateTokenResult

@JsExport data object ValidateTokenSuccess : ValidateTokenResult

@JsExport data object ValidateTokenFailure : ValidateTokenResult

@JsExport sealed interface RefreshTokenResult

@JsExport data class RefreshTokenSuccess(val token: String) : RefreshTokenResult

@JsExport data object RefreshTokenFailure : RefreshTokenResult
