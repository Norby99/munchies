package com.munchies.e2e.support

object ServiceUrls {
  val order = System.getProperty("order.service.url", "http://localhost:8081")
  val user = System.getProperty("user.service.url", "http://localhost:8082")
  val payment = System.getProperty("payment.service.url", "http://localhost:8083")
  val gateway = System.getProperty("gateway.service.url", "http://localhost:8086")
}

object MongoUrls {
  val order = System.getProperty("order.mongo.url", "mongodb://localhost:27018/order-service")
  val user = System.getProperty("user.mongo.url", "mongodb://localhost:27019/user-service")
  val payment = System.getProperty("payment.mongo.url", "mongodb://localhost:27020/payment-service")
  val notification =
    System.getProperty("notification.mongo.url", "mongodb://localhost:27021/notification-service")
  val gateway = System.getProperty("gateway.mongo.url", "mongodb://localhost:27023/gateway-service")
}
