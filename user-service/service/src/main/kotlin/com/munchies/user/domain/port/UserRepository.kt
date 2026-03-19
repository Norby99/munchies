package com.munchies.user.domain.port

import com.munchies.commons.Repository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

interface UserRepository : Repository<UserId, User>
