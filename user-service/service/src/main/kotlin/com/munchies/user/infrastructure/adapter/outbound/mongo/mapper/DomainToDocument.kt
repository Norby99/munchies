package com.munchies.user.infrastructure.adapter.outbound.mongo.mapper

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.mongo.document.UserDocument

object DomainToDocument {
  fun User.toDocument(): UserDocument = UserDocument(id = id.value)
  fun UserDocument.toDomain(): User = User(id = UserId(id))
}
