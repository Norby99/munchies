import { Body, Post, Route, Tags } from "tsoa";
/**
 * HTTP controller exposing gateway endpoints.
 */
@Route("notifications")
@Tags("Notifications")
export class GatewayController {
  constructor() {
    console.log("GatewayService constructor called");
  }

  /**
   * Example.
   */
  @Post()
  public exampleEndpoint() {}
}

@Route("users")
@Tags("Users")
export class UserController {
  /**
   * Logout user endpoint.
   */
  @Post("logout")
  public logout() {}
}
