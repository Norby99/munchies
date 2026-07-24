package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory

import com.munchies.restaurant.domain.aggregate.Category
import com.munchies.restaurant.domain.aggregate.CategoryId
import com.munchies.restaurant.domain.aggregate.Menu
import com.munchies.restaurant.domain.aggregate.MenuId
import com.munchies.restaurant.domain.aggregate.MenuItem
import com.munchies.restaurant.domain.aggregate.MenuItemDetails
import com.munchies.restaurant.domain.aggregate.MenuItemId
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.CategoryDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.MenuDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document.MenuItemDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.MenuValueObjectDocumentFactory.toDocument
import com.munchies.restaurant.infrastructure.adapter.outbound.mongo.factory.MenuValueObjectDocumentFactory.toDomain
import java.math.BigDecimal

object MenuDocumentFactory {

  fun Menu.toDocument(): MenuDocument = MenuDocument(
    id = id.value,
    restaurantId = restaurantId.value,
    name = name.value,
    validity = validity.toDocument(),
    categories = categories.map { it.toDocument() },
  )

  fun MenuDocument.toDomain(): Menu = Menu.fromDatabase(
    id = MenuId(id),
    restaurantId = RestaurantId(restaurantId),
    name = MenuName.of(name),
    categories = categories.map { it.toDomain() },
    validity = validity.toDomain(),
  )

  fun Category.toDocument(): CategoryDocument = CategoryDocument(
    id = id.value,
    name = name.value,
    items = items.map { it.toDocument() },
    variations = variations.map { it.toDocument() },
  )

  fun CategoryDocument.toDomain(): Category = Category(
    id = CategoryId(id),
    name = CategoryName.of(name),
    items = items.map { it.toDomain() },
    variations = variations.map { it.toDomain() },
  )

  fun MenuItem.toDocument(): MenuItemDocument = MenuItemDocument(
    id = id.value,
    name = details.name.value,
    description = details.description.value,
    price = price.amount.toPlainString(),
    validity = validity.toDocument(),
    variations = variations.map { it.toDocument() },
  )

  fun MenuItemDocument.toDomain(): MenuItem = MenuItem(
    id = MenuItemId(id),
    details = MenuItemDetails(
      name = MenuItemName.of(name),
      description = MenuItemDescription.of(description),
    ),
    price = Money(BigDecimal(price)),
    validity = validity.toDomain(),
    variations = variations.map { it.toDomain() },
  )
}
