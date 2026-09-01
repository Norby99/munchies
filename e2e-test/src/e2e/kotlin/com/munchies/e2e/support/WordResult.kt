package com.munchies.e2e.support

import io.micronaut.http.cookie.Cookie

class WordResult {
  var responseStatus: Int? = null
  var responseBody: String? = null
  var authCookie: Cookie? = null
}
