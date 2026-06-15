package com.munchies.user.domain.model

import com.munchies.commons.UUIDEntityId

class TokenId(override val value: String) : UUIDEntityId(value)
