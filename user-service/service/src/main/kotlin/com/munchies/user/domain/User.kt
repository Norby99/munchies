package com.munchies.user.domain

import com.munchies.commons.AggregateRoot

open class User(override val id: UserId) : AggregateRoot<UserId>(id = UserId())
