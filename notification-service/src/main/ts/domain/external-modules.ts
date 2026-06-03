import type * as CommonsModule from "munchies-commons";
import type * as SharedModule from "munchies-notification-service-shared";
import * as UserModule from "munchies-user-service-shared";

const commonsModule = require("munchies-commons") as typeof CommonsModule;
const sharedModule =
  require("munchies-notification-service-shared") as typeof SharedModule;
const userModule = require("munchies-user-service-shared") as typeof UserModule;

const _commons = commonsModule.com.munchies.commons;
const _inbound =
  sharedModule.com.munchies.notification.infrastructure.adapter.inbound;
const _adapter = sharedModule.com.munchies.notification.infrastructure.adapter;
const _userNotification =
  userModule.com.munchies.user.infrastructure.adapter.outbound.notification;

// Values
export const newUUIDEntityId = _commons.newUUIDEntityId;
export const getIdFromEntityId = _commons.getIdFromEntityId;
export const newId = () => getIdFromEntityId(newUUIDEntityId(null));
export const JsEntity = _commons.JsEntity;

// Notification Observers and Subjects
const _UserEmailConfirmationNotification =
  _userNotification.UserEmailConfirmationNotification;
export const UserEmailConfirmationNotification =
  _UserEmailConfirmationNotification;

export type UserEmailConfirmationNotification = InstanceType<
  typeof _UserEmailConfirmationNotification
>;

export const getUserEmailConfirmationNotificationFromJson =
  userModule.com.munchies.user.infrastructure.adapter.outbound.notification
    .userEmailConfirmationNotificationFromJson;

export type _UserEmailConfirmationNotification =
  UserModule.com.munchies.user.infrastructure.adapter.outbound.notification.UserEmailConfirmationNotification;

export const UserEmailConfirmationTopic =
  _userNotification.UserEmailConfirmationNotificationInfo
    .USER_EMAIL_CONFIRMATION_TOPIC;

export const UserEmailConfirmationGroupId =
  _userNotification.UserEmailConfirmationNotificationInfo
    .USER_EMAIL_CONFIRMATION_GROUP_ID;

console.log(userModule.com.munchies.user.infrastructure.adapter);

export const _UserEmailConfirmationNotificationObserver =
  _userNotification.UserEmailConfirmationNotificationObserver;

export type _UserEmailConfirmationNotificationObserver = InstanceType<
  typeof _UserEmailConfirmationNotificationObserver
>;

export const _UserEmailConfirmationNotificationSubject =
  UserModule.com.munchies.user.infrastructure.adapter.outbound.notification
    .UserEmailConfirmationNotificationSubject;

export const UserEmailConfirmationNotificationSubject =
  _userNotification.UserEmailConfirmationNotificationSubject;

// Types
export type JsEntity = InstanceType<typeof JsEntity>;
export type UUIDEntityId = InstanceType<typeof _commons.UUIDEntityId>;
