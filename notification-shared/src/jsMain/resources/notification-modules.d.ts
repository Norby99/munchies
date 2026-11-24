// notification-modules.d.ts
import type { com } from "./munchies-notification-shared";

export type NotificationAPI =
  com.munchies.notification.infrastructure.adapter.inbound.NotificationAPI;
export declare const NotificationAPI: typeof com.munchies.notification.infrastructure.adapter.inbound.NotificationAPI;

export type LiveNotificationsConfig =
  com.munchies.notification.infrastructure.adapter.inbound.LiveNotificationsConfig;
export declare const LiveNotificationsConfig: typeof com.munchies.notification.infrastructure.adapter.inbound.LiveNotificationsConfig;
