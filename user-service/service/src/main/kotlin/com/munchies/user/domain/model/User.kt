package com.munchies.user.domain.model

import com.munchies.commons.Entity

data class User(
  override val id: UserId,
  val profile: UserProfile,
) : Entity<UserId>(id = id)
