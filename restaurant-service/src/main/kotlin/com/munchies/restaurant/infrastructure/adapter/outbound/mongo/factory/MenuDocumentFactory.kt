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
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
class MenuDocumentFactory(private val validityDocumentFactory: ValidityDocumentFactory) {

  fun toDocument(menu: Menu): MenuDocument = MenuDocument(
    id = menu.id.value,
    restaurantId = menu.restaurantId.value,
    name = menu.name.value,
    validity = validityDocumentFactory.toDocument(menu.validity),
    categories = menu.categories.map { it.toDocument() },
  )

  fun toDomain(document: MenuDocument): Menu = Menu.fromDatabase(
    id = MenuId(document.id),
    restaurantId = RestaurantId(document.restaurantId),
    name = MenuName.of(document.name),
    categories = document.categories.map { it.toDomain() },
    validity = validityDocumentFactory.toDomain(document.validity),
  )

  private fun Category.toDocument(): CategoryDocument = CategoryDocument(
    id = id.value,
    name = name.value,
    items = items.map { it.toDocument() },
    variations = variations.map { it.toDocument() },
  )

  private fun CategoryDocument.toDomain(): Category = Category(
    id = CategoryId(id),
    name = CategoryName.of(name),
    items = items.map { it.toDomain() },
    variations = variations.map { it.toDomain() },
  )

  private fun MenuItem.toDocument(): MenuItemDocument = MenuItemDocument(
    id = id.value,
    name = details.name.value,
    description = details.description.value,
    price = price.amount.toPlainString(),
    validity = validityDocumentFactory.toDocument(validity),
    variations = variations.map { it.toDocument() },
  )

  private fun MenuItemDocument.toDomain(): MenuItem = MenuItem(
    id = MenuItemId(id),
    details = MenuItemDetails(
      name = MenuItemName.of(name),
      description = MenuItemDescription.of(description),
    ),
    price = Money(BigDecimal(price)),
    validity = validityDocumentFactory.toDomain(validity),
    variations = variations.map { it.toDomain() },
  )
}
