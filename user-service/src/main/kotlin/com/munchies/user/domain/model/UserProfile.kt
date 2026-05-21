package com.munchies.user.domain.model

/**
 * Represents the profile information of a user in the domain model.
 *
 * @property username The username of the user. This is a unique identifier for the user within the system.
 * @property email The email address of the user. Used for communication and account recovery.
 * @property role The role of the user, represented as a UserRole. Defines the user's permissions and access level.
 */
data class UserProfile(
  val username: String,
  val email: String,
  val role: UserRole,
) {
  companion object {
    /**
     * A predefined empty UserProfile instance.
     *
     * This instance is used as a default value when no profile information is provided.
     * It initializes the username and email as empty strings and sets the role to CUSTOMER.
     */
    val empty: UserProfile = UserProfile(
      username = "",
      email = "",
      role = UserRole.CUSTOMER,
    )
  }
}
