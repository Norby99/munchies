import { Body, Post, Route, Tags } from "tsoa";
import { NotificationAPI } from "munchies-notification-service-shared/kotlin/notification-modules";

/**
 * HTTP controller exposing notification endpoints.
 */
@Route("notifications")
@Tags("Notifications")
export class NotificationController extends NotificationAPI {
  constructor() {
    super();
    console.log("NotificationService constructor called");
  }

  /**
   * Creates a notification.
   */
  @Post()
  public processNotification() {}
}
