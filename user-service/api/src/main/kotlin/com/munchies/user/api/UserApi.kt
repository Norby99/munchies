package com.munchies.user.api

interface UserApi<IdType, E> {
  fun getUser(id: IdType): E
}
