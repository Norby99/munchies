import { SimpleRoute } from "../simple-route";
import { GetUserRoute } from "./get-user.route";
import { RegisterUserRoute } from "./register-user.route";
import { LoginUserRoute } from "./login-user.route";
import { LogoutUserRoute } from "./logout-user.route";
import { UpdateUserInfoRoute } from "./update-user-info.route";
import { UpdateUserPasswordRoute } from "./update-user-password.route";
import { DeleteUserRoute } from "./delete-user.route";
import { VerifyEmailRoute } from "./verify-email.route";

export const userRoutes: SimpleRoute<any>[] = [
  new GetUserRoute(),
  new RegisterUserRoute(),
  new LoginUserRoute(),
  new LogoutUserRoute(),
  new UpdateUserInfoRoute(),
  new UpdateUserPasswordRoute(),
  new DeleteUserRoute(),
  new VerifyEmailRoute(),
];
