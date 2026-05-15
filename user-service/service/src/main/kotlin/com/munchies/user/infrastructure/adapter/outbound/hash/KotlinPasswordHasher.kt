package com.munchies.user.infrastructure.adapter.outbound.hash

import com.munchies.user.domain.port.PasswordHasher
import jakarta.inject.Singleton
import java.security.MessageDigest

@Singleton
interface KotlinPasswordHasher : PasswordHasher {
  override fun hash(password: String, salt: String): String {
    val input = password + salt
    val digest = MessageDigest.getInstance(HASH_ALGORITHM)
    val hashBytes = digest.digest(input.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
  }

  override fun generateSalt(): String {
    val saltBytes = ByteArray(DEFAULT_SALT_LENGTH)
    java.security.SecureRandom().nextBytes(saltBytes)
    return saltBytes.joinToString("") { "%02x".format(it) }
  }

  companion object {
    const val DEFAULT_SALT_LENGTH = 16
    const val SALT_LENGTH = 32
    const val HASH_ALGORITHM = "SHA-256"
  }
}
