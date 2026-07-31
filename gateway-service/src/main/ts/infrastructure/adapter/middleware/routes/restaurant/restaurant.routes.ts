import { SimpleRoute } from "../simple-route";
import { CreateRestaurantRoute } from "./create-restaurant.route";
export const restaurantRoutes: SimpleRoute<any>[] = [
  new CreateRestaurantRoute(),
];
