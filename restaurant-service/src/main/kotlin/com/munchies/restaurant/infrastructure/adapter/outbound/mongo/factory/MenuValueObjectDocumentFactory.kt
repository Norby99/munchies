package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.menu.Variation
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOption
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.VariationOptionDocument
import java.math.BigDecimal

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
}
