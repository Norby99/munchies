package com.munchies.user.infrastructure.adapter.outbound.mail

import com.munchies.user.domain.port.Mailer
import jakarta.inject.Singleton

/**
 * Development-only implementation of [Mailer] that simulates sending email.
 *
 * This fake mailer allows local testing of flows that trigger email sending
 * without requiring an external SMTP service. It performs simple validation
 * of input arguments and returns an appropriate [Mailer.MailerResult].
 */
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
