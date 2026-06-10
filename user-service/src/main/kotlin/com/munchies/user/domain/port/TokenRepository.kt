package com.munchies.user.domain.port

import com.munchies.commons.Repository
import com.munchies.user.domain.model.Token
import com.munchies.user.domain.model.TokenId

interface TokenRepository : Repository<TokenId, Token> {
  fun isRevoked(token: String): Boolean
  fun setRevoked(token: String)
}
