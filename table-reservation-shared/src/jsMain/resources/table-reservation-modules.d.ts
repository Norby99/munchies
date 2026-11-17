// table-reservation-modules.d.ts
import type { com } from "./munchies-table-reservation-shared";

export type ReservationStatus =
  com.munchies.tablereservation.infrastructure.adapter.dto.ReservationStatus;
export declare const ReservationStatus: typeof
  com.munchies.tablereservation.infrastructure.adapter.dto.ReservationStatus;

export type TableReservationDto =
  com.munchies.tablereservation.infrastructure.adapter.dto.TableReservationDto;
export declare const TableReservationDto: typeof
  com.munchies.tablereservation.infrastructure.adapter.dto.TableReservationDto;

export declare const TableReservationServiceConfig: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.web.config.TableReservationServiceConfig;

export type TableReservationAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.TableReservationAPI;
export declare const TableReservationAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.TableReservationAPI;

export type ReserveTableAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsReserveTableAPI;
export declare const ReserveTableAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsReserveTableAPI;

export type GetTableReservationAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsGetTableReservationAPI;
export declare const GetTableReservationAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsGetTableReservationAPI;

export type GetTableReservationsAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsGetTableReservationsAPI;
export declare const GetTableReservationsAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsGetTableReservationsAPI;

export type ModifyTableReservationAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsModifyTableReservationAPI;
export declare const ModifyTableReservationAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsModifyTableReservationAPI;

export type CancelTableReservationAPI =
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsCancelTableReservationAPI;
export declare const CancelTableReservationAPI: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.JsCancelTableReservationAPI;

export type ReserveTableRequest =
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.ReserveTableRequest;
export declare const ReserveTableRequest: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.ReserveTableRequest;

export declare const reserveTableRequestFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.reserveTableRequestFromJson;

export type ModifyTableReservationRequest =
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.ModifyTableReservationRequest;
export declare const ModifyTableReservationRequest: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.ModifyTableReservationRequest;

export declare const modifyTableReservationRequestFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.modifyTableReservationRequestFromJson;

export type CancelTableReservationRequest =
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.CancelTableReservationRequest;
export declare const CancelTableReservationRequest: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.CancelTableReservationRequest;

export declare const cancelTableReservationRequestFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.inbound.request.cancelTableReservationRequestFromJson;

export type TableReservationResponse =
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationResponse;
export declare const TableReservationResponse: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationResponse;

export declare const tableReservationResponseFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.tableReservationResponseFromJson;

export type TableReservationsResponse =
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationsResponse;
export declare const TableReservationsResponse: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.TableReservationsResponse;

export declare const tableReservationsResponseFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.response.tableReservationsResponseFromJson;

export declare const TableReservationNotificationInfo: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationNotificationInfo;

export type TableReservationAcceptedNotification =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotification;
export declare const TableReservationAcceptedNotification: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotification;

export declare const tableReservationAcceptedNotificationFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.tableReservationAcceptedNotificationFromJson;

export type TableReservationAcceptedNotificationObserver =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotificationObserver;
export declare const TableReservationAcceptedNotificationObserver: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotificationObserver;

export type TableReservationAcceptedNotificationSubject =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotificationSubject;
export declare const TableReservationAcceptedNotificationSubject: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationAcceptedNotificationSubject;

export type TableReservationModifiedNotification =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotification;
export declare const TableReservationModifiedNotification: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotification;

export declare const tableReservationModifiedNotificationFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.tableReservationModifiedNotificationFromJson;

export type TableReservationModifiedNotificationObserver =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotificationObserver;
export declare const TableReservationModifiedNotificationObserver: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotificationObserver;

export type TableReservationModifiedNotificationSubject =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotificationSubject;
export declare const TableReservationModifiedNotificationSubject: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationModifiedNotificationSubject;

export type TableReservationCanceledNotification =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotification;
export declare const TableReservationCanceledNotification: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotification;

export declare const tableReservationCanceledNotificationFromJson: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.tableReservationCanceledNotificationFromJson;

export type TableReservationCanceledNotificationObserver =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotificationObserver;
export declare const TableReservationCanceledNotificationObserver: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotificationObserver;

export type TableReservationCanceledNotificationSubject =
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotificationSubject;
export declare const TableReservationCanceledNotificationSubject: typeof
  com.munchies.tablereservation.infrastructure.adapter.outbound.notification.TableReservationCanceledNotificationSubject;
