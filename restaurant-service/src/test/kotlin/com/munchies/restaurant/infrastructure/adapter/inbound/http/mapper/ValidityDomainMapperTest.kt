package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidityDomainMapperTest {
  @Test
  fun `a single-element array maps to the matching leaf input`() {
    val input = arrayOf(ValidityDto(ValidityType.ALWAYS)).toInput()
    assertEquals(ValidityInput.Always, input)
  }

  @Test
  fun `a two-element array folds into a single And`() {
    val weekly = ValidityDto(ValidityType.WEEKLY, mapOf("days" to "1,3,5"))
    val hours = ValidityDto(ValidityType.HOURS, mapOf("start" to "09:00", "end" to "17:00"))
    val input = arrayOf(weekly, hours).toInput()
    assertEquals(
      ValidityInput.And(
        ValidityInput.Weekly(listOf(1, 3, 5)),
        ValidityInput.Hours("09:00", "17:00"),
      ),
      input,
    )
  }

  @Test
  fun `an empty array is rejected rather than silently defaulting`() {
    assertThrows<IllegalArgumentException> { emptyArray<ValidityDto>().toInput() }
  }

  @Test
  fun `a nested And flattens into a flat array, not a nested structure`() {
    val nested = ValidityInput.And(
      ValidityInput.And(ValidityInput.Always, ValidityInput.From("2024-01-01")),
      ValidityInput.Until("2024-12-31"),
    )
    val dtos = nested.toDto()
    assertEquals(3, dtos.size)
    assertEquals(ValidityType.ALWAYS, dtos[0].type)
    assertEquals(ValidityType.FROM, dtos[1].type)
    assertEquals("2024-01-01", dtos[1].value["start"])
    assertEquals(ValidityType.UNTIL, dtos[2].type)
    assertEquals("2024-12-31", dtos[2].value["end"])
  }

  @Test
  fun `round trip through toDto and toInput preserves the rules, regardless of tree shape`() {
    val original = ValidityInput.And(
      ValidityInput.Period("a", "b"),
      ValidityInput.And(ValidityInput.Yearly(1, 2, 3, 4), ValidityInput.Always),
    )
    val roundTripped = original.toDto().toInput()
    // toDto() flattens right-leaning; toInput() re-folds left-leaning — same three rules, a
    // different (but equivalent, for an associative/commutative AND) tree shape is expected.
    assertEquals(
      ValidityInput.And(
        ValidityInput.And(ValidityInput.Period("a", "b"), ValidityInput.Yearly(1, 2, 3, 4)),
        ValidityInput.Always,
      ),
      roundTripped,
    )
  }
}
