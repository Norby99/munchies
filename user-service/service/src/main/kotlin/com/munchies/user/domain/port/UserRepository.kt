package com.munchies.user.domain.port

import com.munchies.commons.Repository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

/**
 * Repository interface for managing User entities.
 *
 * This interface extends the generic Repository interface, providing CRUD operations
 * for User entities identified by a UserId.
 *
 * @see com.munchies.commons.Repository
 */
interface UserRepository : Repository<UserId, User>
