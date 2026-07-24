// restaurant-modules.d.ts
import type { com } from "./munchies-restaurant-shared";

export type RestaurantDto =
  com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto;
export declare const RestaurantDto: typeof
  com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto;



export type CreateRestaurantAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateRestaurantAPI;
export declare const CreateRestaurantAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateRestaurantAPI;

export type CreateRestaurantRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest;
export declare const CreateRestaurantRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest;

export declare const createRestaurantRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.createRestaurantRequestFromJson;

export type CreateRestaurantResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse;
export declare const CreateRestaurantResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse;

export declare const createRestaurantResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.createRestaurantResponseFromJson;



export type GetRestaurantAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetRestaurantAPI;
export declare const GetRestaurantAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetRestaurantAPI;

export type GetRestaurantRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantRequest;
export declare const GetRestaurantRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantRequest;

export type GetRestaurantResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse;
export declare const GetRestaurantResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse;



export type GetManagerRestaurantsAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetManagerRestaurantsAPI;
export declare const GetManagerRestaurantsAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetManagerRestaurantsAPI;

export type GetManagerRestaurantsRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest;
export declare const GetManagerRestaurantsRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsRequest;

export type GetManagerRestaurantsResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse;
export declare const GetManagerRestaurantsResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse;



export type UpdateRestaurantAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateRestaurantAPI;
export declare const UpdateRestaurantAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateRestaurantAPI;

export type UpdateRestaurantRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest;
export declare const UpdateRestaurantRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest;

export type UpdateRestaurantResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse;
export declare const UpdateRestaurantResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse;



export type DeleteRestaurantAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteRestaurantAPI;
export declare const DeleteRestaurantAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteRestaurantAPI;

export type DeleteRestaurantRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest;
export declare const DeleteRestaurantRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantRequest;

export declare const deleteRestaurantRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.deleteRestaurantRequestFromJson;

export type DeleteRestaurantResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse;
export declare const DeleteRestaurantResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse;

export declare const deleteRestaurantResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.deleteRestaurantResponseFromJson;



export type ErrorResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.ErrorResponse;
export declare const ErrorResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.ErrorResponse;

export declare const errorResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.errorResponseFromJson;



// ---- Menu API ----

export type CreateMenuAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateMenuAPI;
export declare const CreateMenuAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateMenuAPI;

export type GetMenuAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetMenuAPI;
export declare const GetMenuAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetMenuAPI;

export type GetRestaurantMenusAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetRestaurantMenusAPI;
export declare const GetRestaurantMenusAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsGetRestaurantMenusAPI;

export type UpdateMenuAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateMenuAPI;
export declare const UpdateMenuAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateMenuAPI;

export type DeleteMenuAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteMenuAPI;
export declare const DeleteMenuAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteMenuAPI;

// ---- Menu ----

export type CreateMenuRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest;
export declare const CreateMenuRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest;

export declare const createMenuRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.createMenuRequestFromJson;

export type CreateMenuResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse;
export declare const CreateMenuResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuResponse;

export declare const createMenuResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.createMenuResponseFromJson;



export type GetMenuRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuRequest;
export declare const GetMenuRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuRequest;

export declare const getMenuRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getMenuRequestFromJson;

export type GetMenuResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse;
export declare const GetMenuResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse;

export declare const getMenuResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getMenuResponseFromJson;



export type GetRestaurantMenusRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusRequest;
export declare const GetRestaurantMenusRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusRequest;

export type GetRestaurantMenusResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse;
export declare const GetRestaurantMenusResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse;

export declare const getRestaurantMenusResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getRestaurantMenusResponseFromJson;



export type UpdateMenuRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest;
export declare const UpdateMenuRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuRequest;

export declare const updateMenuRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.updateMenuRequestFromJson;

export type UpdateMenuResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse;
export declare const UpdateMenuResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.UpdateMenuResponse;

export declare const updateMenuResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.updateMenuResponseFromJson;



export type DeleteMenuResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse;
export declare const DeleteMenuResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.DeleteMenuResponse;

export declare const deleteMenuResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.deleteMenuResponseFromJson;



// ---- Category API ----

export type CreateCategoryAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateCategoryAPI;
export declare const CreateCategoryAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateCategoryAPI;

export type UpdateCategoryAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateCategoryAPI;
export declare const UpdateCategoryAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateCategoryAPI;

export type DeleteCategoryAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteCategoryAPI;
export declare const DeleteCategoryAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsDeleteCategoryAPI;

// ---- Category ----

export type CreateCategoryRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest;
export declare const CreateCategoryRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest;

export declare const createCategoryRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.createCategoryRequestFromJson;

export type CreateCategoryResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse;
export declare const CreateCategoryResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryResponse;

export declare const createCategoryResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.createCategoryResponseFromJson;



export type UpdateCategoryRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest;
export declare const UpdateCategoryRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryRequest;

export declare const updateCategoryRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.updateCategoryRequestFromJson;

export type UpdateCategoryResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse;
export declare const UpdateCategoryResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.UpdateCategoryResponse;

export declare const updateCategoryResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.updateCategoryResponseFromJson;



export type DeleteCategoryResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse;
export declare const DeleteCategoryResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.DeleteCategoryResponse;

export declare const deleteCategoryResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.deleteCategoryResponseFromJson;



// ---- Menu Item API ----

export type CreateMenuItemAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateMenuItemAPI;
export declare const CreateMenuItemAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsCreateMenuItemAPI;

export type UpdateMenuItemAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateMenuItemAPI;
export declare const UpdateMenuItemAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsUpdateMenuItemAPI;

export type RemoveMenuItemAPI =
  com.munchies.restaurant.infrastructure.adapter.inbound.JsRemoveMenuItemAPI;
export declare const RemoveMenuItemAPI: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.JsRemoveMenuItemAPI;

// ---- Menu Item ----

export type CreateMenuItemRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest;
export declare const CreateMenuItemRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest;

export declare const createMenuItemRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.createMenuItemRequestFromJson;

export type CreateMenuItemResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse;
export declare const CreateMenuItemResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemResponse;

export declare const createMenuItemResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.createMenuItemResponseFromJson;



export type UpdateMenuItemRequest =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest;
export declare const UpdateMenuItemRequest: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemRequest;

export declare const updateMenuItemRequestFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.updateMenuItemRequestFromJson;

export type UpdateMenuItemResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse;
export declare const UpdateMenuItemResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.UpdateMenuItemResponse;

export declare const updateMenuItemResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.updateMenuItemResponseFromJson;



export type DeleteMenuItemResponse =
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse;
export declare const DeleteMenuItemResponse: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.DeleteMenuItemResponse;

export declare const deleteMenuItemResponseFromJson: typeof
  com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.deleteMenuItemResponseFromJson;
