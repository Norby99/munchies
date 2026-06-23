package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport

@JsExport
data class DeleteUserRequest(
  val userId: String,
)
