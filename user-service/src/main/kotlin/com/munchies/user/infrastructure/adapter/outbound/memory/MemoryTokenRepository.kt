package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.Token
import com.munchies.user.domain.model.TokenId
import com.munchies.user.domain.port.TokenRepository
import jakarta.inject.Singleton

@Singleton
class MemoryTokenRepository(
  private val repository: InMemoryRepository<TokenId, Token> = InMemoryRepository(),
) : TokenRepository {

  override fun findById(id: TokenId): Token? = repository.findById(id)

  override fun save(entity: Token) = repository.save(entity)

  override fun update(entity: Token) = repository.update(entity)

  override fun delete(entity: Token) = repository.delete(entity)

  override fun isRevoked(token: String): Boolean {
    return repository.findById(TokenId(token))?.isValid ?: true
  }

  override fun setRevoked(token: String) {
    repository.findById(TokenId(token))?.let {
      repository.update(it.copy(isValid = false))
    }
  }
}
