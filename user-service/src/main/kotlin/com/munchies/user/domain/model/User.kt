package com.munchies.user.domain.model

import com.munchies.commons.Entity

/**
 * Represents a User in the domain model.
 *
 * @property id The unique identifier for the user, represented as a UserId.
 * @property profile The profile information associated with the user, represented as a UserProfile.
 */
data class User(
  override val id: UserId,
  val profile: UserProfile,
) : Entity<UserId>(id = id)
