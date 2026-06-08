package com.munchies.user.infrastructure.adapter.outbound.mail

import com.munchies.user.domain.port.Mailer.Companion.MailerResult.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FakeMailerTest {
  @Test
  fun `mailer returns destination not found when destination is empty`() {
    val mailer = FakeMailer()

    mailer.sendMail("", "some contents") shouldBe DestinationNotFound
  }

  @Test
  fun `mailer returns unreadable contents when contents is empty`() {
    val mailer = FakeMailer()

    mailer.sendMail("some destiantion", "") shouldBe UnreadableContents
  }

  @Test
  fun `mailer sends email when destination and content is not empty`() {
    val mailer = FakeMailer()

    mailer.sendMail("some destiantion", "some content") shouldBe MailSent
  }
}
