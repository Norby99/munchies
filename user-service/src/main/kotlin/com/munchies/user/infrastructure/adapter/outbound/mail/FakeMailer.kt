package com.munchies.user.infrastructure.adapter.outbound.mail

import com.munchies.user.domain.port.Mailer
import jakarta.inject.Singleton

@Singleton
class FakeMailer : Mailer {
  override fun sendMail(destination: String, contents: String): Mailer.Companion.MailerResult {
    return when (destination) {
      "" -> Mailer.Companion.MailerResult.DestinationNotFound
      else -> when (contents) {
        "" -> Mailer.Companion.MailerResult.UnreadableContents
        else -> Mailer.Companion.MailerResult.MailSent
      }
    }
  }
}
