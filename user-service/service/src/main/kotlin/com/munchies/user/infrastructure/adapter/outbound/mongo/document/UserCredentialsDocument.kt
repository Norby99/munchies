package com.munchies.user.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

/**
 * Represents the MongoDB document for storing user credentials.
 *
 * This class is annotated with @MappedEntity to indicate that it maps to a MongoDB collection.
 * Each instance of this class corresponds to a document in the collection.
 *
 * @property id The unique identifier for the user credentials document. This is the primary key.
 * @property passwordHash The hashed password of the user, used for authentication.
 * @property salt The cryptographic salt used during password hashing for added security.
 * @property loginAttempts The number of failed login attempts. Used for tracking account lockout policies.
 * @property lockedUntil The timestamp (in milliseconds) until which the account is locked. A value of 0 or negative indicates no lock.
 * @property lastLogin The timestamp (in milliseconds) of the user's last successful login.
 */
@MappedEntity
data class UserCredentialsDocument(
  @field:Id
  val id: String, // Primary key for the document
  val passwordHash: String, // Hashed password for the user
  val salt: String, // Cryptographic salt for the password hash
  val loginAttempts: Int, // Number of failed login attempts
  val lockedUntil: Long, // Timestamp until the account is locked
  val lastLogin: Long, // Timestamp of the last successful login
)
