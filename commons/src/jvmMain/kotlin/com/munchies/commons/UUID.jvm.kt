package com.munchies.commons

actual fun getUUID(): String {
  return java.util.UUID.randomUUID().toString()
}
