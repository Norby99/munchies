package com.munchies.user.domain.port

/**
 * Abstraction for generating salts and hashing passwords within the domain boundary.
 */
interface PasswordHasher {
  fun hash(password: String, salt: String): String
  fun generateSalt(): String
}
