package com.munchies.e2e.support

import io.micronaut.http.cookie.Cookie

class WordResult {
  var responseStatus: Int? = null
  var responseBody: String? = null
  var authCookie: Cookie? = null
  var orderId: String? = null

  /** The restaurant created for the scenario, and the dish an order can be placed for. */
  var restaurantId: String? = null
  var menuItemId: String? = null
}
