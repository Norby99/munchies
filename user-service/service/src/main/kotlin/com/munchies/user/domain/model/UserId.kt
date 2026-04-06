package com.munchies.user.domain.model

import com.munchies.commons.UUIDEntityId

class UserId(value: String = newId()) : UUIDEntityId(value)
