package com.munchies.user.infrastructure.adapter.inbound.request

data class VerifyEmailRequest(val id: String, val otk: String)
