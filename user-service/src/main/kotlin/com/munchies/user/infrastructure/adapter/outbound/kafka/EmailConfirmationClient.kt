package com.munchies.user.infrastructure.adapter.outbound.kafka

import com.munchies.user.infrastructure.adapter.outbound.notification.UserEmailConfirmationNotificationInfo
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.serde.annotation.SerdeImport

@SerdeImport
@KafkaClient
interface EmailConfirmationClient {
  @Topic(UserEmailConfirmationNotificationInfo.USER_EMAIL_CONFIRMATION_TOPIC)
  fun confirmEmail(notification: String)
}
