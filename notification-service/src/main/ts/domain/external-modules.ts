import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-notification-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-notification-service-shared") as typeof SharedModule;

const _commons = commonsModule.com.munchies.commons;
const _inbound =
  sharedModule.com.munchies.notification.infrastructure.adapter.inbound;
const _adapter = sharedModule.com.munchies.notification.infrastructure.adapter;

// Values
export const newUUIDEntityId = _commons.newUUIDEntityId;
export const getIdFromEntityId = _commons.getIdFromEntityId;
export const newId = () => getIdFromEntityId(newUUIDEntityId(null));
export const JsEntity = _commons.JsEntity;

const _NotificationAPI = _inbound.NotificationAPI;

// Types
export type JsEntity = InstanceType<typeof JsEntity>;
export type UUIDEntityId = InstanceType<typeof _commons.UUIDEntityId>;

export type NotificationAPI = InstanceType<typeof _NotificationAPI>;
