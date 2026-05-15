import { Body, Post, Route, Tags } from "tsoa";
import { NotificationAPI } from "@main/domain/external-modules";
/**
 * HTTP controller exposing notification endpoints.
 */
@Route("notifications")
@Tags("Notifications")
export class NotificationController implements NotificationAPI {
  constructor() {
    console.log("NotificationService constructor called");
  }

  /**
   * Creates a notification.
   */
  @Post()
  public processNotification() {}
}
