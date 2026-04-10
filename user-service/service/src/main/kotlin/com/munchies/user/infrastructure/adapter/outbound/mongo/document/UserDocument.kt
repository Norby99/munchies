package com.munchies.user.infrastructure.adapter.outbound.mongo.document

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

/**
 * Represents the MongoDB document for storing user information.
 *
 * This class is annotated with @MappedEntity to indicate that it maps to a MongoDB collection.
 * Each instance of this class corresponds to a document in the collection.
 *
 * @property id The unique identifier for the user document. This is the primary key.
 * @property username The username of the user. This is a unique identifier within the application.
 * @property email The email address of the user. Used for communication and account recovery.
 * @property role The role of the user in the system, represented as a string (e.g., "CUSTOMER", "MANAGER").
 */
@MappedEntity
data class UserDocument(
  @field:Id
  val id: String, // Primary key for the document
  val username: String, // The username of the user
  val email: String, // The email address of the user
  val role: String, // The role of the user in the system
)
