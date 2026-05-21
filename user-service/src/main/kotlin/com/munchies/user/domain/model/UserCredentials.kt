package com.munchies.user.domain.model

import com.munchies.commons.Entity

/**
 * Represents the credentials of a user in the domain model.
 *
 * @property id The unique identifier for the user, represented as a UserId.
 * @property passwordHash The hashed password of the user, used for authentication.
 * @property salt The cryptographic salt used during password hashing for added security.
 * @property loginAttempts The number of failed login attempts. Defaults to 0.
 * @property lockedUntil The timestamp (in milliseconds) until which the account is locked.
 * Defaults to -1L, indicating no lock.
 * @property lastLogin The timestamp (in milliseconds) of the user's last successful login.
 * Defaults to 0L.
 */
data class UserCredentials(
  override val id: UserId,
  val passwordHash: String,
  val salt: String,
  val loginAttempts: Int = 0,
  val lockedUntil: Long = -1L,
  val lastLogin: Long = 0L,
) : Entity<UserId>(id = id)
