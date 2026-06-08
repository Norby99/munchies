package com.munchies.user.domain.model

import com.munchies.commons.Entity

data class Token(override val id: TokenId, val isValid: Boolean) : Entity<TokenId>(id)
