package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport

@JsExport
data class UpdateUserInfoRequest(
  val user: UserDTO,
)
