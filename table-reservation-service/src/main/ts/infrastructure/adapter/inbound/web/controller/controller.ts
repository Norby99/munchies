import {
  Body,
  Get,
  Patch,
  Path,
  Post,
  Put,
  Query,
  Response,
  Route,
  SuccessResponse,
  Tags,
} from "tsoa";
import {
  CancelTableReservationRequest,
  ModifyTableReservationRequest,
  ReserveTableRequest,
  TableReservationDto,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { CancelTableReservation } from "@main/application/port/inbound/CancelTableReservation";
import { GetTableReservation } from "@main/application/port/inbound/GetTableReservation";
import { GetTableReservations } from "@main/application/port/inbound/GetTableReservations";
import { ModifyTableReservation } from "@main/application/port/inbound/ModifyTableReservation";
import { ReserveTable } from "@main/application/port/inbound/ReserveTable";
import { TableReservationDtoFactory } from "@main/infrastructure/adapter/dto/factory/table-reservation-dto-factory";
import {
  ReservationBeans,
  ReservationServices,
} from "@main/infrastructure/adapter/inbound/web/config/ReservationBeans";
import { ReservationError } from "./reservation-error";

export { ReservationError } from "./reservation-error";

/**
 * HTTP controller exposing the table reservation endpoints.
 *
 * It only translates HTTP to and from the application layer: every rule (who may act on a
 * reservation, whether a table is free, what is notified) lives in the use cases behind the
 * inbound ports. A use case failure is raised as a {@link ReservationError} carrying its HTTP
 * status.
 *
 * Responses are documented here as the bare reservation; on the wire they are wrapped in the
 * `{ result, code }` envelope every Munchies service answers with (see index.ts).
 */
@Route("reservations")
@Tags("Table Reservations")
export class TableReservationController {
  private readonly reserveTableUseCase: ReserveTable;
  private readonly getReservationUseCase: GetTableReservation;
  private readonly getReservationsUseCase: GetTableReservations;
  private readonly modifyReservationUseCase: ModifyTableReservation;
  private readonly cancelReservationUseCase: CancelTableReservation;

  constructor(services: ReservationServices = ReservationBeans.getDefaultServices()) {
    this.reserveTableUseCase = services.reserveTable;
    this.getReservationUseCase = services.getTableReservation;
    this.getReservationsUseCase = services.getTableReservations;
    this.modifyReservationUseCase = services.modifyTableReservation;
    this.cancelReservationUseCase = services.cancelTableReservation;
  }

  /**
   * Reserves a table at a restaurant. The reservation is accepted at once when a table is free
   * for the time slot, and refused otherwise.
   *
   * @param request The restaurant, the reserving customer, the arrival time and the number of
   * guests.
   * @returns The accepted reservation, with the table booked for it.
   */
  @Post()
  @SuccessResponse("201", "Reservation accepted")
  @Response("400", "Invalid reservation request")
  @Response("404", "Restaurant not found")
  @Response("409", "No table available for the party at the requested time")
  @Response("503", "restaurant-service unavailable, retry later")
  public async reserveTable(@Body() request: ReserveTableRequest): Promise<TableReservationDto> {
    const result = await this.reserveTableUseCase.execute(
      TableReservationDtoFactory.toReserveCommand(request),
    );
    if (result.type === "SUCCESS") return TableReservationDtoFactory.toDto(result.reservation);
    throw ReservationError.from(result);
  }

  /**
   * Lists reservations, ordered by the start of their time slot.
   *
   * @param customerId Only the reservations of this customer.
   * @param restaurantId Only the reservations at this restaurant.
   * @param managerId The manager asking: requires `restaurantId`, which they must manage.
   */
  @Get()
  @SuccessResponse("200", "The matching reservations")
  @Response("400", "No customer nor restaurant given")
  @Response("403", "The restaurant is managed by someone else")
  @Response("503", "restaurant-service unavailable, retry later")
  public async getReservations(
    @Query() customerId?: string,
    @Query() restaurantId?: string,
    @Query() managerId?: string,
  ): Promise<TableReservationDto[]> {
    const result = await this.getReservationsUseCase.execute({
      customerId,
      restaurantId,
      managerId,
    });
    if (result.type === "SUCCESS") {
      return result.reservations.map(TableReservationDtoFactory.toDto);
    }
    throw ReservationError.from(result);
  }

  /**
   * Reads one reservation.
   *
   * @param id The reservation identifier.
   * @param customerId When given, the reservation must belong to this customer.
   */
  @Get("{id}")
  @SuccessResponse("200", "The reservation")
  @Response("404", "Reservation not found")
  public async getReservation(
    @Path() id: string,
    @Query() customerId?: string,
  ): Promise<TableReservationDto> {
    const result = await this.getReservationUseCase.execute(id, customerId);
    if (result.type === "SUCCESS") return TableReservationDtoFactory.toDto(result.reservation);
    throw ReservationError.from(result);
  }

  /**
   * Moves a reservation to another time and/or changes its number of guests.
   *
   * @param id The reservation identifier.
   * @param request The requesting customer, the new arrival time and the new number of guests.
   * @returns The modified reservation, with the table booked for the new slot.
   */
  @Put("{id}")
  @SuccessResponse("200", "Reservation modified")
  @Response("400", "Invalid arrival time or number of guests")
  @Response("403", "The reservation belongs to another customer")
  @Response("404", "Reservation not found")
  @Response("409", "No table available, or the reservation can no longer change")
  @Response("503", "restaurant-service unavailable, retry later")
  public async modifyReservation(
    @Path() id: string,
    @Body() request: ModifyTableReservationRequest,
  ): Promise<TableReservationDto> {
    const result = await this.modifyReservationUseCase.execute(
      TableReservationDtoFactory.toModifyCommand(id, request),
    );
    if (result.type === "SUCCESS") return TableReservationDtoFactory.toDto(result.reservation);
    throw ReservationError.from(result);
  }

  /**
   * Cancels a reservation, freeing its table.
   *
   * @param id The reservation identifier.
   * @param request The requesting customer.
   * @returns The canceled reservation.
   */
  @Patch("{id}/cancel")
  @SuccessResponse("200", "Reservation canceled")
  @Response("403", "The reservation belongs to another customer")
  @Response("404", "Reservation not found")
  @Response("409", "The reservation is already canceled or has already started")
  public async cancelReservation(
    @Path() id: string,
    @Body() request: CancelTableReservationRequest,
  ): Promise<TableReservationDto> {
    const result = await this.cancelReservationUseCase.execute(id, request.customerId);
    if (result.type === "SUCCESS") return TableReservationDtoFactory.toDto(result.reservation);
    throw ReservationError.from(result);
  }
}
