package com.munchies.commons

actual fun getUUID(): String {
  return kotlin.random.Random.nextLong().toString()
}
