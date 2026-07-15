package com.munchies.user.domain.port

/**
 * Abstraction for sending notification emails from the user domain.
 */
interface Mailer {
  fun sendMail(destination: String, contents: String): MailerResult

  companion object {
    sealed interface MailerResult {
      data object MailSent : MailerResult
      data object DestinationNotFound : MailerResult
      data object UnreadableContents : MailerResult
    }
  }
}
