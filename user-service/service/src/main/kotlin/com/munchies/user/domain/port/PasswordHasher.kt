package com.munchies.user.domain.port

interface PasswordHasher {
  fun hash(password: String, salt: String): String
}
