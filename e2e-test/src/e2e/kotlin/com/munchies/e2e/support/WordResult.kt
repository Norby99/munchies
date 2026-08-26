package com.munchies.e2e.support

import io.micronaut.http.cookie.Cookies

class WordResult {
  var responseStatus: Int? = null
  var responseBody: String? = null
  var cookies: Cookies? = null
}
