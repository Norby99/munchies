package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.Token
import com.munchies.user.domain.model.TokenId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class MemoryTokenRepositoryTest {

  @Test
  fun `repository finds token by its token value`() {
    val repo = getRepository()
    val token = "prova"
    val entity = Token(TokenId(token), isValid = false)
    repo.save(entity)
    repo.findById(TokenId(token)) shouldBe entity
  }

  @Test
  fun `repository updates token revoked state`() {
    val repo = getRepository()
    val token = "prova"
    val entity = Token(TokenId(token), isValid = false)
    repo.save(entity)
    repo.findById(TokenId(token)) shouldBe entity
    repo.update(entity.copy(isValid = true))
    repo.findById(TokenId(token)) shouldNotBe null
    repo.findById(TokenId(token))!!.isValid shouldBe true
  }

  @Test
  fun `repository checks token revoked state`() {
    val repo = getRepository()
    val token = "prova"
    repo.isRevoked(token) shouldBe true

    val entity = Token(TokenId(token), isValid = false)
    repo.save(entity)
    repo.isRevoked(token) shouldBe false
  }

  @Test
  fun `repository sets token revoked state`() {
    val repo = getRepository()
    val token = "prova"
    repo.isRevoked(token) shouldBe true

    val entity = Token(TokenId(token), isValid = true)
    repo.save(entity)
    repo.setRevoked(token)
    repo.isRevoked(token) shouldBe false
  }

  private fun getRepository(): MemoryTokenRepository = object : MemoryTokenRepository {
    override val repository: InMemoryRepository<TokenId, Token> = InMemoryRepository()
  }
}
