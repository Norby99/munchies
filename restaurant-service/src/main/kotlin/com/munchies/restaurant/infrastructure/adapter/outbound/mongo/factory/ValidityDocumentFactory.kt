package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.ValidityData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.ValidityType
import io.micronaut.core.type.Argument
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import java.time.LocalTime

@Singleton
class ValidityDocumentFactory(private val objectMapper: ObjectMapper) {

  // AND has no ValidityType case of its own — a list of N rules already means "all of these must
  // hold," so an outgoing Validity.And flattens into a list on the way out, and an incoming list
  // folds into nested Validity.And pairs (via combine()) on the way in. Every other case maps one
  // Validity leaf <-> one ValidityData directly.

  private fun Validity.toDataList(): List<ValidityData> = when (this) {
    is Validity.And -> first.toDataList() + second.toDataList()
    else -> listOf(toSingleData())
  }

  private fun Validity.toSingleData(): ValidityData = when (this) {
    is Validity.Always -> ValidityData(ValidityType.ALWAYS)
    is Validity.Period -> ValidityData(
      ValidityType.PERIOD,
      mapOf("start" to start.toString(), "end" to end.toString()),
    )
    is Validity.Yearly -> ValidityData(
      ValidityType.YEARLY,
      mapOf(
        "startMonth" to start.monthValue.toString(),
        "startDay" to start.dayOfMonth.toString(),
        "endMonth" to end.monthValue.toString(),
        "endDay" to end.dayOfMonth.toString(),
      ),
    )
    is Validity.Weekly -> ValidityData(
      ValidityType.WEEKLY,
      mapOf("days" to days.map { it.value }.joinToString(",")),
    )
    is Validity.Hours -> ValidityData(
      ValidityType.HOURS,
      mapOf("start" to start.toString(), "end" to end.toString()),
    )
    is Validity.And -> error("AND is flattened by toDataList() before reaching toSingleData()")
  }

  private fun List<ValidityData>.toDomain(): Validity {
    require(isNotEmpty()) { "validity must contain at least one rule" }
    return map { it.toSingleDomain() }.reduce { acc, next -> acc.combine(next) }
  }

  private fun ValidityData.toSingleDomain(): Validity = when (type) {
    ValidityType.ALWAYS -> Validity.always
    ValidityType.PERIOD -> Validity.period(value.getValue("start"), value.getValue("end"))
    ValidityType.YEARLY -> Validity.yearly(
      value.getValue("startMonth").toInt(),
      value.getValue("startDay").toInt(),
      value.getValue("endMonth").toInt(),
      value.getValue("endDay").toInt(),
    )
    ValidityType.WEEKLY -> Validity.weekly(
      value.getValue("days").split(",").filter { it.isNotEmpty() }.map { it.toInt() },
    )
    ValidityType.HOURS -> Validity.hours(
      LocalTime.parse(value.getValue("start")),
      LocalTime.parse(value.getValue("end")),
    )
  }

  fun toDocument(validity: Validity): String =
    objectMapper.writeValueAsString(LIST_ARGUMENT, validity.toDataList())

  fun toDomain(json: String): Validity = objectMapper.readValue(json, LIST_ARGUMENT).toDomain()

  companion object {
    private val LIST_ARGUMENT: Argument<List<ValidityData>> = Argument.listOf(
      ValidityData::class.java,
    )
  }
}
