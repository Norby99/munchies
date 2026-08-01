package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LoginUserResult")
data class LoginUserResult(
  val id: String,
  val role: AuthRole,
)

@JsExport
@Serializable
@SerialName("LoginUserResponse")
open class LoginUserResponse(
  override val result: LoginUserResult,
  override val code: Int = 200,
) : WebResponse<LoginUserResult>() {
  override fun toJson(): String = Json.encodeToString(this)

  @JsName("createWithIdAndRole")
  constructor(id: String, role: AuthRole, code: Int = 200) : this(
    LoginUserResult(id, role),
    code,
  )
}

@JsExport
fun loginUserResponseFromJson(json: String): LoginUserResponse = Json.decodeFromString(json)
