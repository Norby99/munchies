package com.munchies.restaurant.domain.aggregate

import com.munchies.commons.AggregateRoot
import com.munchies.commons.Entity
import com.munchies.commons.UUIDEntityId
import com.munchies.restaurant.domain.valueobject.Money
import com.munchies.restaurant.domain.valueobject.RestaurantId
import com.munchies.restaurant.domain.valueobject.menu.CategoryName
import com.munchies.restaurant.domain.valueobject.menu.MenuItemDescription
import com.munchies.restaurant.domain.valueobject.menu.MenuItemName
import com.munchies.restaurant.domain.valueobject.menu.MenuName
import com.munchies.restaurant.domain.valueobject.menu.Validity
import com.munchies.restaurant.domain.valueobject.menu.VariationName
import com.munchies.restaurant.domain.valueobject.menu.VariationOptionName
import java.time.LocalDateTime

data class VariationOption(
  val name: VariationOptionName,
  val additionalPrice: Money,
)

data class VariationId(override val value: String = newId()) : UUIDEntityId(value)

class Variation internal constructor(
  override val id: VariationId = VariationId(),
  var name: VariationName,
  var options: List<VariationOption>,
) : Entity<VariationId>(id) {
  fun update(name: VariationName, options: List<VariationOption>) {
    this.name = name
    this.options = options
  }
}

data class MenuItemId(override val value: String = newId()) : UUIDEntityId(value)

class MenuItem internal constructor(
  override val id: MenuItemId = MenuItemId(),
  var name: MenuItemName,
  var description: MenuItemDescription,
  var price: Money,
  val variations: MutableList<Variation> = mutableListOf(),
  var validity: Validity = Validity.always,
) : Entity<MenuItemId>(id) {
  fun update(name: MenuItemName, description: MenuItemDescription, price: Money) {
    this.name = name
    this.description = description
    this.price = price
  }

  fun addVariation(name: VariationName, options: List<VariationOption>): Variation {
    val variation = Variation(name = name, options = options)
    variations.add(variation)
    return variation
  }

  fun updateVariation(
    variationId: VariationId,
    name: VariationName,
    options: List<VariationOption>,
  ) {
    val variation = variations.find { it.id == variationId }
      ?: throw IllegalArgumentException("Variation not found")
    variation.update(name, options)
  }

  fun removeVariation(variationId: VariationId) {
    variations.removeIf { it.id == variationId }
  }

  fun isValid(date: LocalDateTime): Boolean {
    return validity.isValid(date)
  }

  fun updateValidity(validity: Validity) {
    this.validity = validity
  }
}

data class CategoryId(override val value: String = newId()) : UUIDEntityId(value)

class Category internal constructor(
  override val id: CategoryId = CategoryId(),
  var name: CategoryName,
  val items: MutableList<MenuItem> = mutableListOf(),
  val variations: MutableList<Variation> = mutableListOf(),
) : Entity<CategoryId>(id) {
  fun updateName(name: CategoryName) {
    this.name = name
  }

  fun addItem(name: MenuItemName, description: MenuItemDescription, price: Money): MenuItem {
    val item = MenuItem(name = name, description = description, price = price)
    items += item
    return item
  }

  fun updateItem(
    itemId: MenuItemId,
    name: MenuItemName,
    description: MenuItemDescription,
    price: Money,
  ) {
    val item = items.find { it.id == itemId } ?: throw IllegalArgumentException(
      "Item not found",
    )
    item.update(name, description, price)
  }

  fun removeItem(itemId: MenuItemId) {
    items.removeIf { it.id == itemId }
  }

  fun addVariation(name: VariationName, options: List<VariationOption>): Variation {
    val variation = Variation(name = name, options = options)
    variations.add(variation)
    return variation
  }

  fun updateVariation(
    variationId: VariationId,
    name: VariationName,
    options: List<VariationOption>,
  ) {
    val variation = variations.find { it.id == variationId }
      ?: throw IllegalArgumentException("Variation not found")
    variation.update(name, options)
  }

  fun removeVariation(variationId: VariationId) {
    variations.removeIf { it.id == variationId }
  }
}

data class MenuId(override val value: String = newId()) : UUIDEntityId(value)

class Menu internal constructor(
  override val id: MenuId,
  val restaurantId: RestaurantId,
  var name: MenuName = MenuName.of("Menu Default"),
  categories: List<Category> = emptyList(),
  var validity: Validity = Validity.always,
) : AggregateRoot<MenuId>(id) {
  private val _categories = categories.toMutableList()

  val categories get(): List<Category> = _categories.toList()

  fun updateName(name: MenuName) {
    this.name = name
  }

  fun addCategory(name: CategoryName): Category {
    val category = Category(name = name)
    _categories.add(category)
    return category
  }

  fun updateCategory(categoryId: CategoryId, name: CategoryName) {
    val category = _categories.find {
      it.id == categoryId
    } ?: throw IllegalArgumentException("Category not found")
    category.updateName(name)
  }

  fun removeCategory(categoryId: CategoryId) {
    _categories.removeIf { it.id == categoryId }
  }

  fun getCategory(categoryId: CategoryId): Category? {
    return _categories.find { it.id == categoryId }
  }

  fun isValid(date: LocalDateTime): Boolean {
    return validity.isValid(date)
  }

  fun updateValidity(validity: Validity) {
    this.validity = validity
  }

  companion object {
    fun create(restaurantId: RestaurantId): Menu = Menu(id = MenuId(), restaurantId = restaurantId)

    fun create(restaurantId: RestaurantId, name: MenuName): Menu =
      Menu(id = MenuId(), restaurantId = restaurantId, name = name)

    fun create(restaurantId: RestaurantId, name: MenuName, validity: Validity): Menu =
      Menu(id = MenuId(), restaurantId = restaurantId, name = name, validity = validity)

    fun fromDatabase(
      id: MenuId,
      restaurantId: RestaurantId,
      name: MenuName,
      categories: List<Category>,
      validity: Validity,
    ): Menu = Menu(
      id = id,
      restaurantId = restaurantId,
      name = name,
      categories = categories,
      validity = validity,
    )
  }
}
