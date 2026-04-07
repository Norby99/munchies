package com.munchies.user.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity
data class UserCredentialsDocument(
  @field:Id
  val id: String,
  val passwordHash: String,
  val salt: String,
  val loginAttempts: Int,
  val lockedUntil: Long,
  val lastLogin: Long,
)
