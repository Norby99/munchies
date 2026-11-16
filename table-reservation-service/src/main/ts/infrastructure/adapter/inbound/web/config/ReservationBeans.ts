import { TableBooker } from "@main/application/booking/TableBooker";
import { CancelTableReservation } from "@main/application/port/inbound/CancelTableReservation";
import { GetTableReservation } from "@main/application/port/inbound/GetTableReservation";
import { GetTableReservations } from "@main/application/port/inbound/GetTableReservations";
import { ModifyTableReservation } from "@main/application/port/inbound/ModifyTableReservation";
import { ReserveTable } from "@main/application/port/inbound/ReserveTable";
import { CancelTableReservationUseCase } from "@main/application/usecase/CancelTableReservationUseCase";
import { GetTableReservationUseCase } from "@main/application/usecase/GetTableReservationUseCase";
import { GetTableReservationsUseCase } from "@main/application/usecase/GetTableReservationsUseCase";
import { ModifyTableReservationUseCase } from "@main/application/usecase/ModifyTableReservationUseCase";
import { ReserveTableUseCase } from "@main/application/usecase/ReserveTableUseCase";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { Logger } from "@main/domain/port/logger";
import { RestaurantServiceClient } from "@main/domain/port/restaurant-service-client";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";
import { KafkaTableReservationNotificationPublisher } from "@main/infrastructure/adapter/outbound/kafka/KafkaTableReservationNotificationPublisher";
import { PinoLogger } from "@main/infrastructure/adapter/outbound/logging/logger";
import { InMemoryTableReservationRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryTableReservationRepository";
import { TableReservationMongoRepository } from "@main/infrastructure/adapter/outbound/mongo/repository/table-reservation-mongo-repository";
import { RestaurantServiceHttpClient } from "@main/infrastructure/adapter/outbound/restaurant/RestaurantServiceHttpClient";

/** The inbound ports the web adapter depends on. */
export interface ReservationServices {
  reserveTable: ReserveTable;
  getTableReservation: GetTableReservation;
  getTableReservations: GetTableReservations;
  modifyTableReservation: ModifyTableReservation;
  cancelTableReservation: CancelTableReservation;
}

/** The outbound adapters the use cases are wired with. */
export interface ReservationAdapters {
  repository: TableReservationRepository;
  restaurantServiceClient: RestaurantServiceClient;
  notificationPublisher: TableReservationNotificationPublisher;
  logger: Logger;
  policy?: ReservationPolicy;
  now?: () => Date;
}

/**
 * Composition root of the service: builds the use cases and wires them with their outbound
 * adapters.
 */
export class ReservationBeans {
  private static instance: ReservationServices | null = null;

  /** Wires the use cases with the given adapters; tests pass fakes here. */
  static createServices(adapters: ReservationAdapters): ReservationServices {
    const { repository, restaurantServiceClient, notificationPublisher, logger } = adapters;
    const policy = adapters.policy ?? new ReservationPolicy();
    const now = adapters.now ?? (() => new Date());
    const tableBooker = new TableBooker(repository, restaurantServiceClient);

    return {
      reserveTable: new ReserveTableUseCase(
        tableBooker,
        notificationPublisher,
        logger,
        policy,
        now,
      ),
      getTableReservation: new GetTableReservationUseCase(repository),
      getTableReservations: new GetTableReservationsUseCase(
        repository,
        restaurantServiceClient,
        logger,
      ),
      modifyTableReservation: new ModifyTableReservationUseCase(
        repository,
        tableBooker,
        notificationPublisher,
        logger,
        policy,
        now,
      ),
      cancelTableReservation: new CancelTableReservationUseCase(
        repository,
        notificationPublisher,
        logger,
        now,
      ),
    };
  }

  /**
   * The production wiring. Reservations are stored in MongoDB when `MONGODB_URI` is set, in
   * memory otherwise. The slot length comes from `RESERVATION_DURATION_MINUTES` (default: two
   * hours).
   */
  static createDefaultServices(env: NodeJS.ProcessEnv = process.env): ReservationServices {
    return ReservationBeans.createServices({
      repository: env.MONGODB_URI
        ? new TableReservationMongoRepository()
        : new InMemoryTableReservationRepository(),
      restaurantServiceClient: new RestaurantServiceHttpClient(),
      notificationPublisher: new KafkaTableReservationNotificationPublisher(),
      logger: new PinoLogger(),
      policy: ReservationBeans.policyFrom(env),
    });
  }

  /** @throws InvalidReservationError if the configured duration is not usable. */
  static policyFrom(env: NodeJS.ProcessEnv = process.env): ReservationPolicy {
    const configured = env.RESERVATION_DURATION_MINUTES;
    return configured === undefined || configured.trim() === ""
      ? new ReservationPolicy()
      : new ReservationPolicy(Number(configured));
  }

  static getDefaultServices(): ReservationServices {
    if (!this.instance) {
      this.instance = this.createDefaultServices();
    }
    return this.instance;
  }

  static resetInstance(): void {
    this.instance = null;
  }
}
