package com.munchies.user.infrastructure.adapter.outbound.hash

import com.munchies.user.domain.port.PasswordHasher
import jakarta.inject.Singleton
import java.security.MessageDigest

@Singleton
interface KotlinPasswordHasher : PasswordHasher {
  override fun hash(password: String, salt: String): String {
    val input = password + salt
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
  }
}
