package com.munchies.user.infrastructure.adapter.outbound.hash

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.security.MessageDigest
import org.junit.jupiter.api.Test

class KotlinPasswordHasherTest {

  private val hasher = object : KotlinPasswordHasher {}

  @Test
  fun `hash should return consistent hash for same input`() {
    val password = "password123"
    val salt = "randomSalt"
    val hash1 = hasher.hash(password, salt)
    val hash2 = hasher.hash(password, salt)
    hash1 shouldBe hash2
  }

  @Test
  fun `hash should produce different hashes for different salts`() {
    val password = "password123"
    val salt1 = "salt1"
    val salt2 = "salt2"
    val hash1 = hasher.hash(password, salt1)
    val hash2 = hasher.hash(password, salt2)
    hash1 shouldNotBe hash2
  }

  @Test
  fun `hash should produce different hashes for different passwords`() {
    val password1 = "password123"
    val password2 = "password456"
    val salt = "randomSalt"
    val hash1 = hasher.hash(password1, salt)
    val hash2 = hasher.hash(password2, salt)
    hash1 shouldNotBe hash2
  }

  @Test
  fun `hash should handle empty password and salt`() {
    val password = ""
    val salt = ""
    val hash = hasher.hash(password, salt)
    val expectedHash = MessageDigest.getInstance("SHA-256")
      .digest("".toByteArray())
      .joinToString("") { "%02x".format(it) }
    hash shouldBe expectedHash
  }

  @Test
  fun `hash should handle long password and salt`() {
    val password = "a".repeat(1000)
    val salt = "b".repeat(1000)
    val hash = hasher.hash(password, salt)
    hash.length shouldBe 64
  }
}
