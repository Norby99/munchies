package com.munchies.user.adapter.outbound.mongo.mapper

import com.munchies.user.adapter.outbound.mongo.document.UserDocument
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

object DomainToDocument {
  fun User.toDocument(): UserDocument = UserDocument(id = id.value)
  fun UserDocument.toDomain(): User = User(id = UserId(id))
}
