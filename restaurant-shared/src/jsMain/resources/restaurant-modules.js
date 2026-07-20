// restaurant-modules.js
const generated = require("./munchies-restaurant-shared.js");
const _restaurant = generated.com.munchies.restaurant;
const _adapter = _restaurant.infrastructure.adapter;
const _inbound = _adapter.inbound;
const _httpRestaurant = _inbound.http.restaurant;
const _httpMenu = _inbound.http.menu;
const _httpMenuCategory = _httpMenu.category;
const _httpMenuMenuItem = _httpMenu.menuitem;
const _dto = _adapter.dto;

module.exports = {
  RestaurantDto: _dto.RestaurantDto,

  // ---- Restaurant API ----
  CreateRestaurantAPI: _inbound.JsCreateRestaurantAPI,
  CreateRestaurantRequest: _httpRestaurant.CreateRestaurantRequest,
  createRestaurantRequestFromJson: _httpRestaurant.createRestaurantRequestFromJson,
  CreateRestaurantResponse: _httpRestaurant.CreateRestaurantResponse,
  createRestaurantResponseFromJson: _httpRestaurant.createRestaurantResponseFromJson,

  GetRestaurantAPI: _inbound.JsGetRestaurantAPI,
  GetRestaurantRequest: _httpRestaurant.GetRestaurantRequest,
  GetRestaurantResponse: _httpRestaurant.GetRestaurantResponse,

  GetManagerRestaurantsAPI: _inbound.JsGetManagerRestaurantsAPI,
  GetManagerRestaurantsRequest: _httpRestaurant.GetManagerRestaurantsRequest,
  GetManagerRestaurantsResponse: _httpRestaurant.GetManagerRestaurantsResponse,

  UpdateRestaurantAPI: _inbound.JsUpdateRestaurantAPI,
  UpdateRestaurantRequest: _httpRestaurant.UpdateRestaurantRequest,
  UpdateRestaurantResponse: _httpRestaurant.UpdateRestaurantResponse,

  DeleteRestaurantAPI: _inbound.JsDeleteRestaurantAPI,
  DeleteRestaurantRequest: _httpRestaurant.DeleteRestaurantRequest,
  deleteRestaurantRequestFromJson: _httpRestaurant.deleteRestaurantRequestFromJson,
  DeleteRestaurantResponse: _httpRestaurant.DeleteRestaurantResponse,
  deleteRestaurantResponseFromJson: _httpRestaurant.deleteRestaurantResponseFromJson,

  // ---- Error ----
  ErrorResponse: _inbound.http.ErrorResponse,
  errorResponseFromJson: _inbound.http.errorResponseFromJson,

  // ---- Menu API ----
  CreateMenuAPI: _inbound.JsCreateMenuAPI,
  GetMenuAPI: _inbound.JsGetMenuAPI,
  GetRestaurantMenusAPI: _inbound.JsGetRestaurantMenusAPI,
  UpdateMenuAPI: _inbound.JsUpdateMenuAPI,
  DeleteMenuAPI: _inbound.JsDeleteMenuAPI,

  // ---- Menu ----
  CreateMenuRequest: _httpMenu.CreateMenuRequest,
  createMenuRequestFromJson: _httpMenu.createMenuRequestFromJson,
  CreateMenuResponse: _httpMenu.CreateMenuResponse,
  createMenuResponseFromJson: _httpMenu.createMenuResponseFromJson,

  GetMenuRequest: _httpMenu.GetMenuRequest,
  getMenuRequestFromJson: _httpMenu.getMenuRequestFromJson,
  GetMenuResponse: _httpMenu.GetMenuResponse,
  getMenuResponseFromJson: _httpMenu.getMenuResponseFromJson,

  GetRestaurantMenusRequest: _httpMenu.GetRestaurantMenusRequest,
  GetRestaurantMenusResponse: _httpMenu.GetRestaurantMenusResponse,
  getRestaurantMenusResponseFromJson: _httpMenu.getRestaurantMenusResponseFromJson,

  UpdateMenuRequest: _httpMenu.UpdateMenuRequest,
  updateMenuRequestFromJson: _httpMenu.updateMenuRequestFromJson,
  UpdateMenuResponse: _httpMenu.UpdateMenuResponse,
  updateMenuResponseFromJson: _httpMenu.updateMenuResponseFromJson,

  DeleteMenuResponse: _httpMenu.DeleteMenuResponse,
  deleteMenuResponseFromJson: _httpMenu.deleteMenuResponseFromJson,

  // ---- Category API ----
  CreateCategoryAPI: _inbound.JsCreateCategoryAPI,
  UpdateCategoryAPI: _inbound.JsUpdateCategoryAPI,
  DeleteCategoryAPI: _inbound.JsDeleteCategoryAPI,

  // ---- Category ----
  CreateCategoryRequest: _httpMenuCategory.CreateCategoryRequest,
  createCategoryRequestFromJson: _httpMenuCategory.createCategoryRequestFromJson,
  CreateCategoryResponse: _httpMenuCategory.CreateCategoryResponse,
  createCategoryResponseFromJson: _httpMenuCategory.createCategoryResponseFromJson,

  UpdateCategoryRequest: _httpMenuCategory.UpdateCategoryRequest,
  updateCategoryRequestFromJson: _httpMenuCategory.updateCategoryRequestFromJson,
  UpdateCategoryResponse: _httpMenuCategory.UpdateCategoryResponse,
  updateCategoryResponseFromJson: _httpMenuCategory.updateCategoryResponseFromJson,

  DeleteCategoryResponse: _httpMenuCategory.DeleteCategoryResponse,
  deleteCategoryResponseFromJson: _httpMenuCategory.deleteCategoryResponseFromJson,

  // ---- Menu Item API ----
  CreateMenuItemAPI: _inbound.JsCreateMenuItemAPI,
  UpdateMenuItemAPI: _inbound.JsUpdateMenuItemAPI,
  RemoveMenuItemAPI: _inbound.JsRemoveMenuItemAPI,

  // ---- Menu Item ----
  CreateMenuItemRequest: _httpMenuMenuItem.CreateMenuItemRequest,
  createMenuItemRequestFromJson: _httpMenuMenuItem.createMenuItemRequestFromJson,
  CreateMenuItemResponse: _httpMenuMenuItem.CreateMenuItemResponse,
  createMenuItemResponseFromJson: _httpMenuMenuItem.createMenuItemResponseFromJson,

  UpdateMenuItemRequest: _httpMenuMenuItem.UpdateMenuItemRequest,
  updateMenuItemRequestFromJson: _httpMenuMenuItem.updateMenuItemRequestFromJson,
  UpdateMenuItemResponse: _httpMenuMenuItem.UpdateMenuItemResponse,
  updateMenuItemResponseFromJson: _httpMenuMenuItem.updateMenuItemResponseFromJson,

  DeleteMenuItemResponse: _httpMenuMenuItem.DeleteMenuItemResponse,
  deleteMenuItemResponseFromJson: _httpMenuMenuItem.deleteMenuItemResponseFromJson,
}
