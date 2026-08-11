package com.munchies.restaurant.infrastructure.adapter.inbound.http.config

import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import io.micronaut.context.ApplicationContext
import io.micronaut.serde.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals


class ValiditySerdeTest {
  @Test
  fun `ValidityDto round-trips through Micronaut Serde using the enum's plain constant name`() {
    ApplicationContext.run().use { ctx ->
      val mapper = ctx.getBean(ObjectMapper::class.java)
      val dto = ValidityDto(ValidityType.WEEKLY, mapOf("days" to "1,3,5"))

      val json = mapper.writeValueAsString(dto)
      assertEquals("""{"type":"WEEKLY","value":{"days":"1,3,5"}}""", json)

      val decoded = mapper.readValue(json, ValidityDto::class.java)
      assertEquals(ValidityType.WEEKLY, decoded.type)
      assertEquals(mapOf("days" to "1,3,5"), decoded.value)
    }
  }

  @Test
  fun `a value-less ValidityDto (ALWAYS) round-trips using the default empty map`() {
    ApplicationContext.run().use { ctx ->
      val mapper = ctx.getBean(ObjectMapper::class.java)
      val dto = ValidityDto(ValidityType.ALWAYS)

      val json = mapper.writeValueAsString(dto)
      val decoded = mapper.readValue(json, ValidityDto::class.java)
      assertEquals(ValidityType.ALWAYS, decoded.type)
      assertEquals(emptyMap(), decoded.value)
    }
  }
}
