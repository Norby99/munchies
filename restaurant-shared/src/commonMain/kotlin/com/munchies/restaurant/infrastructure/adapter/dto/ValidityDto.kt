package com.munchies.restaurant.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
sealed interface ValidityDto {
  val type: String
}

@JsExport
@Serializable
class PeriodValidity(val start: String, val end: String) : ValidityDto {
  override val type = "period"
}

@JsExport
@Serializable
class YearlyValidity(
  val startMonth: Int,
  val startDay: Int,
  val endMonth: Int,
  val endDay: Int,
) : ValidityDto {
  override val type = "yearly"
}

@JsExport
@Serializable
class WeeklyValidity(val days: Array<Int>) : ValidityDto {
  override val type = "weekly"
}

@JsExport
@Serializable
class FromValidity(val start: String) : ValidityDto {
  override val type = "from"
}

@JsExport
@Serializable
class UntilValidity(val end: String) : ValidityDto {
  override val type = "until"
}

@JsExport
@Serializable
object AlwaysValidity : ValidityDto {
  override val type = "always"
}

@JsExport
@Serializable
class HoursValidity(val start: String, val end: String) : ValidityDto {
  override val type = "hours"
}

@JsExport
@Serializable
class AndValidity(val first: ValidityDto, val second: ValidityDto) : ValidityDto {
  override val type = "and"
}
