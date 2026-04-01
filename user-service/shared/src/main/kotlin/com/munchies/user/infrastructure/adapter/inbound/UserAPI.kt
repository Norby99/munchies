package com.munchies.user.infrastructure.adapter.inbound

sealed interface UserAPI {
  companion object {
    interface GetUser<UserId, User> : UserAPI {
      fun getUser(id: UserId): User
    }
    interface AddUser<UserId> : UserAPI {
      fun addUser(): UserId
    }
  }
}
