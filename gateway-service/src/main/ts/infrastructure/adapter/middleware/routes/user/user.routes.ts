import { RouteDefinition } from "../route-definition";
import { GetUserRoute } from "./get-user.route";
import { LoginUserRoute } from "./login-user.route";
import { RegisterUserRoute } from "./register-user.route";

export const userRoutes: RouteDefinition<any, any>[] = [
  new GetUserRoute(),
  new RegisterUserRoute(),
  new LoginUserRoute()
];
