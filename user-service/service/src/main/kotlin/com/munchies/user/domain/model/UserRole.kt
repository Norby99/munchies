package com.munchies.user.domain.model

/**
 * Enum class representing the roles a user can have in the system.
 *
 * Roles define the level of access and permissions a user has within the application.
 * - CUSTOMER: Represents a standard user with basic access.
 * - MANAGER: Represents a user with managerial privileges.
 */
enum class UserRole {
  CUSTOMER,
  MANAGER,
  ;

  companion object {
    /**
     * Extension function to convert a string to a UserRole.
     *
     * This function takes a string, converts it to uppercase, and maps it to the corresponding UserRole.
     * If the string does not match any predefined role, it defaults to CUSTOMER.
     *
     * @receiver The string representation of the user role.
     * @return The corresponding UserRole. Defaults to CUSTOMER if no match is found.
     */
    fun String.toUserRole(): UserRole = when (this.uppercase()) {
      "MANAGER" -> MANAGER
      else -> CUSTOMER
    }
  }
}
