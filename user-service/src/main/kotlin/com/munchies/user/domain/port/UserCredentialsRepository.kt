package com.munchies.user.domain.port

import com.munchies.commons.Repository
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId

/**
 * Repository interface for managing UserCredentials entities.
 *
 * This interface extends the generic Repository interface, providing CRUD operations
 * for UserCredentials entities identified by a UserId.
 *
 * @see com.munchies.commons.Repository
 */
interface UserCredentialsRepository : Repository<UserId, UserCredentials>
