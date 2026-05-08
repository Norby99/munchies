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
