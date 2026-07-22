package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.ValidityDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationOptionDocument
import java.math.BigDecimal
import java.time.LocalTime

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

  fun Validity.toDocument(): ValidityDocument = when (this) {
    is Validity.Always -> ValidityDocument.always
    is Validity.Period -> ValidityDocument(
      type = "period",
      start = start.toString(), end = end.toString(),
      startMonth = null, startDay = null, endMonth = null, endDay = null,
      days = null, startHour = null, endHour = null,
      first = null, second = null,
    )
    is Validity.Yearly -> ValidityDocument(
      type = "yearly",
      start = null, end = null,
      startMonth = start.monthValue, startDay = start.dayOfMonth,
      endMonth = end.monthValue, endDay = end.dayOfMonth,
      days = null, startHour = null, endHour = null,
      first = null, second = null,
    )
    is Validity.Weekly -> ValidityDocument(
      type = "weekly",
      start = null, end = null,
      startMonth = null, startDay = null, endMonth = null, endDay = null,
      days = days.map { it.value }, startHour = null, endHour = null,
      first = null, second = null,
    )
    is Validity.Hours -> ValidityDocument(
      type = "hours",
      start = null, end = null,
      startMonth = null, startDay = null, endMonth = null, endDay = null,
      days = null, startHour = start.toString(), endHour = end.toString(),
      first = null, second = null,
    )
    is Validity.And -> ValidityDocument(
      type = "and",
      start = null, end = null,
      startMonth = null, startDay = null, endMonth = null, endDay = null,
      days = null, startHour = null, endHour = null,
      first = first.toDocument(), second = second.toDocument(),
    )
  }

  fun ValidityDocument.toDomain(): Validity = when (type) {
    "always" -> Validity.always
    "period" -> Validity.period(start!!, end!!)
    "yearly" -> Validity.yearly(startMonth!!, startDay!!, endMonth!!, endDay!!)
    "weekly" -> Validity.weekly(days!!.map { it })
    "hours" -> Validity.hours(LocalTime.parse(startHour!!), LocalTime.parse(endHour!!))
    "and" -> first!!.toDomain().combine(second!!.toDomain())
    else -> Validity.always
  }
}
