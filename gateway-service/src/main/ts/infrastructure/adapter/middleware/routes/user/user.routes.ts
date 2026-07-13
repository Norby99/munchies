import { RouteDefinition } from "../route-definition";
import { DeleteUserRoute } from "./delete-user.route";
import { GetUserRoute } from "./get-user.route";
import { LoginUserRoute } from "./login-user.route";
import { RegisterUserRoute } from "./register-user.route";
import { UpdateUserInfoRoute } from "./update-user-info.route";
import { UpdateUserPasswordRoute } from "./update-user-password.route";
import { VerifyEmailRoute } from "./verify-email.route";

export const userRoutes: RouteDefinition<any, any>[] = [
  new GetUserRoute(),
  new RegisterUserRoute(),
  new LoginUserRoute(),
  new UpdateUserInfoRoute(),
  new UpdateUserPasswordRoute(),
  new DeleteUserRoute(),
  new VerifyEmailRoute(),
];
