package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport

@JsExport
data class GetUserResponse(
  val user: UserDTO?,
)
