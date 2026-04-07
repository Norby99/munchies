package com.munchies.user.domain.model

import com.munchies.commons.Entity

data class UserCredentials(
  override val id: UserId,
  val passwordHash: String,
  val salt: String,
  val loginAttempts: Int = 0,
  val lockedUntil: Long = -1L,
  val lastLogin: Long = 0L,
) : Entity<UserId>(id = id)
