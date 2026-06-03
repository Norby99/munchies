import { Body, Post, Route, Tags } from "tsoa";
/**
 * HTTP controller exposing notification endpoints.
 */
@Route("notifications")
@Tags("Notifications")
export class NotificationController {
  constructor() {
    console.log("NotificationService constructor called");
  }

  /**
   * Creates a notification.
   */
  @Post()
  public processNotification() {}
}
