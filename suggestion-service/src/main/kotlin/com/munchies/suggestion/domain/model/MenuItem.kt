package com.munchies.suggestion.domain.model

import com.munchies.commons.AggregateRoot

data class MenuItem(
  override val id: MenuItemId,
  val name: String,
  val description: String,
  val price: Double,
) : AggregateRoot<MenuItemId>(id = id)
