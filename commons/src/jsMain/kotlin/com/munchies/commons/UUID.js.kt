package com.munchies.commons

@JsExport
actual fun getUUID(): String {
  return kotlin.random.Random.nextLong(Long.MAX_VALUE).toString()
}
