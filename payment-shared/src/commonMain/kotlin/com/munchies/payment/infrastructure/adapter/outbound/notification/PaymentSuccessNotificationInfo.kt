package com.munchies.payment.infrastructure.adapter.outbound.notification

import kotlin.js.JsExport

@JsExport
object PaymentSuccessNotificationInfo {
  const val PAYMENT_ID_KEY = "payment_id_key"
  const val ORDER_ID_KEY = "order_id_key"
  const val AMOUNT_KEY = "amount_key"
  const val CURRENCY_KEY = "currency_key"
  const val PAYMENT_SUCCESS_TOPIC = "payment_success_topic"
  const val PAYMENT_SUCCESS_GROUP_ID = "payment_success_group_id"
}
