import { SimpleRoute } from "../simple-route";
import { CreateRestaurantRoute } from "./create-restaurant.route";
import { GetRestaurantRoute } from "./get-restaurant.route";
import { GetManagerRestaurantsRoute } from "./get-manager-restaurants.route";
import { UpdateRestaurantRoute } from "./update-restaurant.route";
import { DeleteRestaurantRoute } from "./delete-restaurant.route";
import { CreateMenuRoute } from "./create-menu.route";
import { GetMenuRoute } from "./get-menu.route";
import { GetRestaurantMenusRoute } from "./get-restaurant-menus.route";
import { UpdateMenuRoute } from "./update-menu.route";
import { DeleteMenuRoute } from "./delete-menu.route";
import { CreateCategoryRoute } from "./create-category.route";
import { UpdateCategoryRoute } from "./update-category.route";
import { DeleteCategoryRoute } from "./delete-category.route";
import { CreateMenuItemRoute } from "./create-menu-item.route";
import { UpdateMenuItemRoute } from "./update-menu-item.route";
import { RemoveMenuItemRoute } from "./remove-menu-item.route";

export const restaurantRoutes: SimpleRoute<any>[] = [
  new CreateRestaurantRoute(),
  new GetRestaurantRoute(),
  new GetManagerRestaurantsRoute(),
  new UpdateRestaurantRoute(),
  new DeleteRestaurantRoute(),
  new CreateMenuRoute(),
  new GetMenuRoute(),
  new GetRestaurantMenusRoute(),
  new UpdateMenuRoute(),
  new DeleteMenuRoute(),
  new CreateCategoryRoute(),
  new UpdateCategoryRoute(),
  new DeleteCategoryRoute(),
  new CreateMenuItemRoute(),
  new UpdateMenuItemRoute(),
  new RemoveMenuItemRoute(),
];
