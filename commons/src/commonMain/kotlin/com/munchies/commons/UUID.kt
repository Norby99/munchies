package com.munchies.commons
/***
 * A common EntityId implementation that uses UUID strings as identifiers.
 * This class can be extended by entities that require a unique identifier.
 *
 * @param value The UUID string value of the identifier. If not provided, a new UUID will be generated.
 */
open class UUIDEntityId(value: String = newId()) : EntityId<String>(value) {
  companion object {
    fun newId(): String = getUUID()
  }
}

expect fun getUUID(): String
