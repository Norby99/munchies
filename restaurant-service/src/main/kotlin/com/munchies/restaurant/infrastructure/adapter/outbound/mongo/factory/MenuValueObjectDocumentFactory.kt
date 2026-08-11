package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.ValidityData
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationOptionDocument
import java.math.BigDecimal
import java.time.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MenuValueObjectDocumentFactory {

  fun Variation.toDocument(): VariationDocument = VariationDocument(
    name = name.value,
    options = options.map { it.toDocument() },
  )

  fun VariationDocument.toDomain(): Variation = Variation(
    name = VariationName(name),
    options = options.map { it.toDomain() },
  )

  fun VariationOption.toDocument(): VariationOptionDocument = VariationOptionDocument(
    name = name,
    additionalPrice = additionalPrice.amount.toPlainString(),
  )

  fun VariationOptionDocument.toDomain(): VariationOption = VariationOption(
    name = name,
    additionalPrice = Money(BigDecimal(additionalPrice)),
  )

  private fun Validity.toData(): ValidityData = when (this) {
    is Validity.Always -> ValidityData.Always
    is Validity.Period -> ValidityData.Period(start.toString(), end.toString())
    is Validity.Yearly -> ValidityData.Yearly(start.monthValue, start.dayOfMonth, end.monthValue, end.dayOfMonth)
    is Validity.Weekly -> ValidityData.Weekly(days.map { it.value })
    is Validity.Hours -> ValidityData.Hours(start.toString(), end.toString())
    is Validity.And -> ValidityData.And(first.toData(), second.toData())
  }

  private fun ValidityData.toDomain(): Validity = when (this) {
    is ValidityData.Always -> Validity.always
    is ValidityData.Period -> Validity.period(start, end)
    is ValidityData.Yearly -> Validity.yearly(startMonth, startDay, endMonth, endDay)
    is ValidityData.Weekly -> Validity.weekly(days)
    is ValidityData.Hours -> Validity.hours(LocalTime.parse(start), LocalTime.parse(end))
    is ValidityData.And -> first.toDomain().combine(second.toDomain())
  }

  fun Validity.toDocument(): String = Json.encodeToString(toData())

  fun String.toDomain(): Validity = Json.decodeFromString<ValidityData>(this).toDomain()
}
