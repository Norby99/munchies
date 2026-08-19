package com.munchies.e2e.support

class TestWorld {
  val orderMongo = MongoTestSupport(MongoUrls.order)
  val notificationMongo = MongoTestSupport(MongoUrls.notification)

  var customerId: String? = null
  var orderId: String? = null

  val createdOrderIds = mutableListOf<String>()
}
