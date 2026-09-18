# Event sourcing in Express.js

Worked example for an event-sourced `Ticket` aggregate, following `payment-service`'s package layout
(`src/main/ts/<layer>/...`, path alias `@main/...`). Adapt names/fields to whatever aggregate you're actually
implementing.

## 1. Events — `domain/model/TicketEvent.ts`

```typescript
export abstract class TicketEvent {
  abstract readonly eventType: string;
  constructor(
    readonly ticketId: string,
    readonly occurredAt: Date = new Date(),
  ) {}
}

export class TicketCreated extends TicketEvent {
  readonly eventType = "TicketCreated";
  constructor(
    ticketId: string,
    readonly orderId: string,
    readonly items: TicketItem[],
  ) {
    super(ticketId);
  }
}

export class TicketAccepted extends TicketEvent {
  readonly eventType = "TicketAccepted";
  constructor(
    ticketId: string,
    readonly readyBy: Date,
  ) {
    super(ticketId);
  }
}

export class TicketCompleted extends TicketEvent {
  readonly eventType = "TicketCompleted";
  constructor(ticketId: string) {
    super(ticketId);
  }
}
```

## 2. Aggregate — `domain/model/Ticket.ts`

```typescript
export enum TicketStatus {
  CREATED = "CREATED",
  ACCEPTED = "ACCEPTED",
  COMPLETED = "COMPLETED",
}

export class Ticket {
  ticketId!: string;
  status!: TicketStatus;
  readyBy?: Date;

  // Used for replay: an empty instance events get applied onto.
  static empty(): Ticket {
    return new Ticket();
  }

  // Used for first creation: process() has no prior state to validate against.
  static create(ticketId: string, orderId: string, items: TicketItem[]): TicketEvent[] {
    return [new TicketCreated(ticketId, orderId, items)];
  }

  // process(): validates against current state, returns events, never mutates.
  accept(readyBy: Date): TicketEvent[] {
    if (this.status !== TicketStatus.CREATED) {
      throw new Error(`Ticket ${this.ticketId} cannot be accepted from status ${this.status}`);
    }
    return [new TicketAccepted(this.ticketId, readyBy)];
  }

  complete(): TicketEvent[] {
    if (this.status !== TicketStatus.ACCEPTED) {
      throw new Error(`Ticket ${this.ticketId} cannot be completed from status ${this.status}`);
    }
    return [new TicketCompleted(this.ticketId)];
  }

  // apply(): mutates state from a fact that already happened. Cannot fail.
  apply(event: TicketEvent): void {
    if (event instanceof TicketCreated) {
      this.ticketId = event.ticketId;
      this.status = TicketStatus.CREATED;
    } else if (event instanceof TicketAccepted) {
      this.status = TicketStatus.ACCEPTED;
      this.readyBy = event.readyBy;
    } else if (event instanceof TicketCompleted) {
      this.status = TicketStatus.COMPLETED;
    }
  }
}
```

## 3. Event store port — `domain/port/ticket-event-store.ts`

```typescript
import { TicketEvent } from "@main/domain/model/TicketEvent";

export interface TicketEventStore {
  /** Appends events starting at expectedSequence; rejects on a concurrent append (sequence conflict). */
  append(ticketId: string, expectedSequence: number, events: TicketEvent[]): Promise<void>;

  /** Loads the full ordered event stream for ticketId. Empty array if the aggregate doesn't exist yet. */
  load(ticketId: string): Promise<TicketEvent[]>;
}
```

Keep this separate from any cross-service publisher port — the store is the source of truth, publication is a side
effect triggered after a successful append.

## 4. Use case — `application/usecase/AcceptTicketUseCase.ts`

```typescript
import { AcceptTicket } from "@main/application/port/inbound/AcceptTicket";
import { Ticket } from "@main/domain/model/Ticket";
import { TicketEventStore } from "@main/domain/port/ticket-event-store";

export class AcceptTicketUseCase implements AcceptTicket {
  constructor(private readonly eventStore: TicketEventStore) {}

  async accept(ticketId: string, readyBy: Date): Promise<void> {
    const history = await this.eventStore.load(ticketId);
    if (history.length === 0) throw new Error(`Ticket ${ticketId} not found`);

    const ticket = Ticket.empty();
    history.forEach((event) => ticket.apply(event));

    const newEvents = ticket.accept(readyBy);
    newEvents.forEach((event) => ticket.apply(event));

    await this.eventStore.append(ticketId, history.length, newEvents);
  }
}
```

## 5. Mongo adapter — `infrastructure/adapter/outbound/mongo/`

`document/ticket-event-document.ts`:

```typescript
export interface TicketEventDocument {
  aggregateId: string;
  sequence: number;
  eventType: string;
  eventVersion: number;
  payload: Record<string, unknown>;
  occurredAt: Date;
}
```

`factory/ticket-event-factory.ts` maps `TicketEvent <-> TicketEventDocument` (serialize/deserialize `payload`,
switch on `eventType` to construct the right event class — this is also where upcasting old `eventVersion` payloads
to the current shape happens, see the pitfalls section of `SKILL.md`).

`repository/ticket-event-mongo-store.ts`:

```typescript
import { Collection } from "mongodb";
import { TicketEvent } from "@main/domain/model/TicketEvent";
import { TicketEventStore } from "@main/domain/port/ticket-event-store";
import { TicketEventDocument } from "../document/ticket-event-document";
import { toDocument, toEvent } from "../factory/ticket-event-factory";

export class TicketEventMongoStore implements TicketEventStore {
  constructor(private readonly collection: Collection<TicketEventDocument>) {}

  async append(ticketId: string, expectedSequence: number, events: TicketEvent[]): Promise<void> {
    const docs = events.map((event, i) => toDocument(ticketId, expectedSequence + i, event));
    // Unique index on (aggregateId, sequence) turns a concurrent append into a duplicate-key error here.
    await this.collection.insertMany(docs);
  }

  async load(ticketId: string): Promise<TicketEvent[]> {
    const docs = await this.collection
      .find({ aggregateId: ticketId })
      .sort({ sequence: 1 })
      .toArray();
    return docs.map(toEvent);
  }
}
```

Create the unique index `(aggregateId, sequence)` alongside the collection's other index setup, same place
`payment-service`'s `infrastructure/adapter/outbound/mongo/config/db.ts` sets up its own.

## Cross-service publication (only if needed)

If another service needs to consume `TicketEvent`s, add a `domain/port/ticket-notification-publisher.ts` and a
`infrastructure/adapter/outbound/kafka/KafkaTicketEventPublisher.ts` implementing it, following
`payment-service`'s existing `KafkaPaymentNotificationPublisher` (lazy producer connection, one topic, JSON
payload). Call the publisher from the use case, after a successful `append()`, not instead of it — see
`infrastructure/adapter/outbound/kafka/KafkaPaymentNotificationPublisher.ts` for the reference implementation
already in this repo.

Remember CLAUDE.md's rule when this adapter pulls in a `-shared` tarball dependency (e.g. for a shared event
schema/topic name constant): strip the `integrity` field from its `package-lock.json` entry, since the tarball is
rebuilt on every local build and the hash goes stale otherwise.
