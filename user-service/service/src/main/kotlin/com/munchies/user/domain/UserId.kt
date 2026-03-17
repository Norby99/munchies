package com.munchies.user.domain

import com.munchies.commons.UUIDEntityId

class UserId(value: String = newId()) : UUIDEntityId(value)
