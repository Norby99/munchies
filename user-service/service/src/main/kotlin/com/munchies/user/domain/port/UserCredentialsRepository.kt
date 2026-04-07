package com.munchies.user.domain.port

import com.munchies.commons.Repository
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId

interface UserCredentialsRepository : Repository<UserId, UserCredentials>
