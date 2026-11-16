// notification-modules.js
const generated = require("./munchies-notification-shared.js");
const _notification = generated.com.munchies.notification;

module.exports = {
  NotificationAPI: _notification.infrastructure.adapter.inbound.NotificationAPI,
  LiveNotificationsConfig: _notification.infrastructure.adapter.inbound.LiveNotificationsConfig,
};
