import { Body, Post, Route, Tags } from "tsoa";
import { logger } from "../../../middleware/logger";
/**
 * HTTP controller exposing gateway endpoints.
 */
@Route("notifications")
@Tags("Notifications")
export class GatewayController {
  constructor() {
    logger.debug("GatewayController created");
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
