package com.munchies.user.domain.model

/**
 * Value object representing a user email address and its verification state.
 */
data class Email(
  val address: String,
  val isVerified: Boolean = false,
)
