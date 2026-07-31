import { RouteDefinition } from "../route-definition";
import { SimpleRoute } from "../simple-route";
import { GetUserRoute } from "./get-user.route";
import { RegisterUserRoute } from "./register-user.route";

export const userRoutes: SimpleRoute<any>[] = [
  new GetUserRoute(),
  new RegisterUserRoute(),
];

