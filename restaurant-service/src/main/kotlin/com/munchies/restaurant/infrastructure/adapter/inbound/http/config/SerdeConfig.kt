package com.munchies.restaurant.infrastructure.adapter.inbound.http.config

import com.munchies.restaurant.infrastructure.adapter.inbound.http.ErrorResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Singleton

@SerdeImport(ErrorResponse::class)
@SerdeImport(CreateMenuRequest::class)
@SerdeImport(CreateMenuResponse::class)
@SerdeImport(UpdateMenuRequest::class)
@SerdeImport(UpdateMenuResponse::class)
@SerdeImport(GetMenuResponse::class)
@SerdeImport(GetRestaurantMenusResponse::class)
@SerdeImport(DeleteMenuResponse::class)
@SerdeImport(CreateCategoryRequest::class)
@SerdeImport(CreateCategoryResponse::class)
@SerdeImport(UpdateCategoryRequest::class)
@SerdeImport(UpdateCategoryResponse::class)
@SerdeImport(DeleteCategoryResponse::class)
@SerdeImport(CreateMenuItemRequest::class)
@SerdeImport(CreateMenuItemResponse::class)
@SerdeImport(UpdateMenuItemRequest::class)
@SerdeImport(UpdateMenuItemResponse::class)
@SerdeImport(DeleteMenuItemResponse::class)
@SerdeImport(CreateRestaurantRequest::class)
@SerdeImport(CreateRestaurantResponse::class)
@SerdeImport(UpdateRestaurantRequest::class)
@SerdeImport(UpdateRestaurantResponse::class)
@SerdeImport(DeleteRestaurantRequest::class)
@SerdeImport(DeleteRestaurantResponse::class)
@SerdeImport(GetRestaurantResponse::class)
@SerdeImport(GetManagerRestaurantsRequest::class)
@SerdeImport(GetManagerRestaurantsResponse::class)
@Singleton
class SerdeConfig
