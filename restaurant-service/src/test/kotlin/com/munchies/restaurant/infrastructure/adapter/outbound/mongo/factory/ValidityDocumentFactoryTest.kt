package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.valueobject.menu.Validity
import io.micronaut.context.ApplicationContext
import java.io.IOException
import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidityDocumentFactoryTest {
  @Test
  fun `always validity round-trips through the blob`() {
    withFactory { factory ->
      assertEquals(Validity.always, factory.toDomain(factory.toDocument(Validity.always)))
    }
  }

  @Test
  fun `period validity round-trips through the blob`() {
    withFactory { factory ->
      val validity = Validity.period("2024-01-01", "2024-12-31")
      assertEquals(validity, factory.toDomain(factory.toDocument(validity)))
    }
  }

  @Test
  fun `yearly validity round-trips through the blob`() {
    withFactory { factory ->
      val validity = Validity.yearly(6, 1, 8, 31)
      assertEquals(validity, factory.toDomain(factory.toDocument(validity)))
    }
  }

  @Test
  fun `weekly validity round-trips through the blob`() {
    withFactory { factory ->
      val validity = Validity.weekly(listOf(1, 3, 5))
      assertEquals(validity, factory.toDomain(factory.toDocument(validity)))
    }
  }

  @Test
  fun `hours validity round-trips through the blob`() {
    withFactory { factory ->
      val validity = Validity.hours(LocalTime.of(9, 0), LocalTime.of(17, 0))
      assertEquals(validity, factory.toDomain(factory.toDocument(validity)))
    }
  }

  @Test
  fun `combined and validity round-trips through the blob, nesting included`() {
    withFactory { factory ->
      val validity = Validity.weekly(listOf(1, 3, 5))
        .combine(Validity.hours(LocalTime.of(18, 0), LocalTime.of(22, 0)))
        .combine(Validity.period("2024-01-01", "2024-12-31"))
      assertEquals(validity, factory.toDomain(factory.toDocument(validity)))
    }
  }

  @Test
  fun `an unrecognized type discriminator fails loudly instead of defaulting to always valid`() {
    withFactory { factory ->
      assertThrows<IOException> {
        factory.toDomain("""[{"type":"whenever"}]""")
      }
    }
  }

  @Test
  fun `malformed json fails loudly instead of silently becoming always valid`() {
    withFactory { factory ->
      assertThrows<IOException> {
        factory.toDomain("not json")
      }
    }
  }

  private fun withFactory(block: (ValidityDocumentFactory) -> Unit) {
    ApplicationContext.run().use { ctx -> block(ctx.getBean(ValidityDocumentFactory::class.java)) }
  }
}
