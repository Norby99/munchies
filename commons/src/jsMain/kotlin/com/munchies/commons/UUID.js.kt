package com.munchies.commons

@JsExport
actual fun getUUID(): String {
  return kotlin.random.Random.nextLong(Long.MAX_VALUE).toString()
}

@JsExport
@JsName("newUUIDEntityId")
fun newUUIDEntityId(value: String?): UUIDEntityId =
  if (value == null) UUIDEntityId() else UUIDEntityId(value)

@JsExport
@JsName("getIdFromEntityId")
fun getIdFromEntityId(entityId: UUIDEntityId): String = entityId.value

@JsExport
@JsName("JsEntity")
class JsEntity(id: UUIDEntityId)
