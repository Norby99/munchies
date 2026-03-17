package com.munchies.user.repository

import com.munchies.commons.InMemoryRepository
import com.munchies.user.domain.User
import com.munchies.user.domain.UserId
import jakarta.inject.Singleton

@Singleton
class UserRepository : InMemoryRepository<UserId, User>()
