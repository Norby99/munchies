package com.munchies.restaurant.infrastructure.adapter.inbound.http.mapper

import com.munchies.restaurant.application.usecase.menu.ValidityInput
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityType

fun Array<ValidityDto>.toInput(): ValidityInput {
  require(isNotEmpty()) { "validity must contain at least one rule" }
  return map { it.toSingleInput() }.reduce { acc, next -> ValidityInput.And(acc, next) }
}

private fun ValidityDto.toSingleInput(): ValidityInput = when (type) {
  ValidityType.PERIOD -> ValidityInput.Period(value.getValue("start"), value.getValue("end"))
  ValidityType.YEARLY -> ValidityInput.Yearly(
    value.getValue("startMonth").toInt(),
    value.getValue("startDay").toInt(),
    value.getValue("endMonth").toInt(),
    value.getValue("endDay").toInt(),
  )
  ValidityType.WEEKLY -> ValidityInput.Weekly(
    value.getValue("days").split(",").filter { it.isNotEmpty() }.map { it.toInt() },
  )
  ValidityType.HOURS -> ValidityInput.Hours(value.getValue("start"), value.getValue("end"))
  ValidityType.FROM -> ValidityInput.From(value.getValue("start"))
  ValidityType.UNTIL -> ValidityInput.Until(value.getValue("end"))
  ValidityType.ALWAYS -> ValidityInput.Always
}

fun ValidityInput.toDto(): Array<ValidityDto> = when (this) {
  is ValidityInput.And -> first.toDto() + second.toDto()
  else -> arrayOf(toSingleDto())
}

private fun ValidityInput.toSingleDto(): ValidityDto = when (this) {
  is ValidityInput.Period -> ValidityDto(ValidityType.PERIOD, mapOf("start" to start, "end" to end))
  is ValidityInput.Yearly -> ValidityDto(
    ValidityType.YEARLY,
    mapOf(
      "startMonth" to startMonth.toString(),
      "startDay" to startDay.toString(),
      "endMonth" to endMonth.toString(),
      "endDay" to endDay.toString(),
    ),
  )
  is ValidityInput.Weekly -> ValidityDto(
    ValidityType.WEEKLY,
    mapOf("days" to days.joinToString(",")),
  )
  is ValidityInput.Hours -> ValidityDto(ValidityType.HOURS, mapOf("start" to start, "end" to end))
  is ValidityInput.From -> ValidityDto(ValidityType.FROM, mapOf("start" to start))
  is ValidityInput.Until -> ValidityDto(ValidityType.UNTIL, mapOf("end" to end))
  is ValidityInput.Always -> ValidityDto(ValidityType.ALWAYS)
  is ValidityInput.And -> error("AND is flattened by toDto() before reaching toSingleDto()")
}
