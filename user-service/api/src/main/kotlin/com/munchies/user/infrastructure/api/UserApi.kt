package com.munchies.user.infrastructure.api

interface UserApi<IdType, E> {
  fun getUser(id: IdType): E
}
