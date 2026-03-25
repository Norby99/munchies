package com.munchies.user.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity
data class UserDocument(
  @field:Id
  val id: String,
)
