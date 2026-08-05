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
import com.munchies.restaurant.domain.valueobject.menu.Variation
import java.time.LocalDateTime

data class MenuItemId(override val value: String = newId()) : UUIDEntityId(value)

data class MenuItemDetails(val name: MenuItemName, val description: MenuItemDescription)

class MenuItem internal constructor(
  override val id: MenuItemId = MenuItemId(),
  var details: MenuItemDetails,
  var price: Money,
  variations: List<Variation> = emptyList(),
  var validity: Validity = Validity.always,
) : Entity<MenuItemId>(id) {
  private val _variations = variations.toMutableList()
  val variations: List<Variation> get() = _variations.toList()
  val name get() = details.name
  val description get() = details.description

  fun update(
    details: MenuItemDetails,
    price: Money,
    newVariations: List<Variation> = _variations,
    validity: Validity = this.validity,
  ) {
    this.details = details
    this.price = price
    this._variations.clear()
    this._variations.addAll(newVariations)
    this.validity = validity
  }

  fun isValid(date: LocalDateTime): Boolean {
    return validity.isValid(date)
  }
}

data class CategoryId(override val value: String = newId()) : UUIDEntityId(value)

class Category internal constructor(
  override val id: CategoryId = CategoryId(),
  var name: CategoryName,
  items: List<MenuItem> = emptyList(),
  variations: List<Variation> = emptyList(),
) : Entity<CategoryId>(id) {
  private val _items = items.toMutableList()
  val items: List<MenuItem> get() = _items.toList()

  private val _variations = variations.toMutableList()
  val variations: List<Variation> get() = _variations.toList()

  fun update(name: CategoryName, newVariations: List<Variation>) {
    this.name = name
    this._variations.clear()
    this._variations.addAll(newVariations)
  }

  fun createItem(
    details: MenuItemDetails,
    price: Money,
    variations: List<Variation> = emptyList(),
    validity: Validity = Validity.always,
  ): MenuItem {
    val item = MenuItem(
      details = details,
      price = price,
      variations = variations,
      validity = validity,
    )
    _items += item
    return item
  }

  fun updateItem(
    itemId: MenuItemId,
    details: MenuItemDetails,
    price: Money,
    newVariations: List<Variation> = emptyList(),
    validity: Validity = Validity.always,
  ): MenuItem {
    val item = _items.find { it.id == itemId } ?: throw IllegalArgumentException(
      "Item not found",
    )
    item.update(details, price, newVariations, validity)
    return item
  }

  fun removeItem(itemId: MenuItemId) {
    _items.removeIf { it.id == itemId }
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

  fun createCategory(name: CategoryName, variations: List<Variation> = emptyList()): Category {
    val category = Category(name = name, variations = variations)
    _categories.add(category)
    return category
  }

  fun updateCategory(
    categoryId: CategoryId,
    name: CategoryName,
    newVariations: List<Variation> = emptyList(),
  ) {
    val category = _categories.find {
      it.id == categoryId
    } ?: throw IllegalArgumentException("Category not found")
    category.update(name, newVariations)
  }

  fun deleteCategory(categoryId: CategoryId) {
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
    ): Menu = Menu(id, restaurantId, name, categories, validity)
  }
}
