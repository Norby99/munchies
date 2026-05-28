package com.munchies.user.domain.model

data class Email(
  val address: String,
  val isVerified: Boolean = false,
)
