package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import kotlinx.serialization.Serializable

@Serializable
sealed interface ValidityData {
  @Serializable
  data object Always : ValidityData

  @Serializable
  data class Period(val start: String, val end: String) : ValidityData

  @Serializable
  data class Yearly(val startMonth: Int, val startDay: Int, val endMonth: Int, val endDay: Int) : ValidityData

  @Serializable
  data class Weekly(val days: List<Int>) : ValidityData

  @Serializable
  data class Hours(val start: String, val end: String) : ValidityData

  @Serializable
  data class And(val first: ValidityData, val second: ValidityData) : ValidityData
}
