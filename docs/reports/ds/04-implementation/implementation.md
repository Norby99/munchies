# Implementation

This chapter describes the concrete choices that realize the design. Limits that come from the code rather than from
the design are listed under [Known gaps](../02-requirements/requirements.md#known-gaps).

## Network protocols

| Link                                 | Protocol                     | Why                                                                  |
|--------------------------------------|------------------------------|----------------------------------------------------------------------|
| Browser to web server                | HTTP (HTTPS _planned_)       | Natively supported by browsers, and by cookies                       |
| Web server to gateway                | HTTP                         | The browser only talks to one origin, so no CORS is needed           |
| Gateway to services                  | HTTP                         | Synchronous request/response, with one contract for all stacks       |
| `payment-service` to `order-service` | HTTP                         | Same as above: the payment needs an immediate outcome                |
| Producers and consumers to Kafka     | Kafka binary protocol on TCP | Durable and asynchronous delivery to `notification-service`          |
| Services to MongoDB                  | MongoDB wire protocol on TCP | The native protocol of the database, handled by the drivers          |
| Services to the tracing backend      | OTLP on HTTP                 | Standard protocol for exporting traces, independent from the backend |
| Prometheus to services               | HTTP                         | Metrics are pulled from `/metrics` or `/prometheus`                  |

REST on HTTP is used wherever the caller needs an answer, and it was preferred to gRPC because the same API has to be
reachable from a browser, and to be described with OpenAPI (`NFR18`). Kafka was chosen because it keeps messages on
disk, so a consumer can catch up after a restart (`NFR6`).

WebSockets are not used yet: there is no push towards the browser yet, so a client sees a new order status the next time
it reloads the data.

Each service listens on port 8080 inside its pod, and is reached through the DNS name of its Kubernetes `Service`, so
no address is hard-coded.

## Data representation

| Data              | Format         | Encoded by                                       |
|-------------------|----------------|--------------------------------------------------|
| REST bodies       | JSON           | Shared classes, through `kotlinx.serialization`  |
| Kafka messages    | JSON strings   | Shared classes, through `kotlinx.serialization`  |
| MongoDB documents | BSON           | The drivers, from Kotlin or TypeScript documents |
| Traces            | Protobuf       | The OpenTelemetry exporters                      |
| API descriptions  | OpenAPI (YAML) | Generated from the code at build time            |

JSON was chosen since it is natively handled by browsers and by Node.js, and because it can be read in logs while
debugging. Its size is not an issue here, as messages hold few fields. Binary formats such as Protocol Buffers would
need a separate schema compiler, while here the shared classes already give the same guarantee on the structure of
the data (see [technological details](technological-details.md)).

All services encode through the same JSON configuration, with `encodeDefaults = true`. Without it, a field holding its
default value would be dropped when the gateway decodes a request and encodes it again to forward it, and the receiving
service would find it missing.

Responses share the same envelope, with a `result` and a `code`:

```json
{
  "result": "invalid token",
  "code": 401
}
```

Kafka messages follow event-carried state transfer, so they hold whatever the consumer needs:

```json
{
  "order_id_key": "6f1c...",
  "restaurant_id_key": "a02e...",
  "customer_id_key": "c91b...",
  "status_key": "READY"
}
```

## Database queries

All data is stored in MongoDB, a document database with no SQL layer. Documents fit aggregates well: an order view
or a menu is read and written as a whole, so there are no joins to perform, and each service only has to query its own
database (`NFR9`).

The services use three styles of access, depending on how much control the query needs:

| Style               | Used by                              | Used for                                          |
|---------------------|--------------------------------------|---------------------------------------------------|
| Micronaut Data      | `user-service`, `restaurant-service` | Plain CRUD on current state                       |
| MongoDB sync driver | `order-service`                      | Event log and views, with explicit indexes (CQRS) |
| Mongoose            | `payment-service`                    | Payment documents                                 |

`order-service` needs the driver directly, because its queries depend on indexes and on specific error codes. The event
log is an append-only collection: an order is read back by its id, sorted by sequence, and a write is an ordered insert.

```kotlin
collection.find(Filters.eq(OrderEventDocument.AGGREGATE_ID, orderId.value))
    .sort(Sorts.ascending(OrderEventDocument.SEQUENCE))
```

The unique index on `(aggregateId, sequence)` turns this insert into an optimistic concurrency check: if two writers
start from the same version, the database accepts the first one and the second gets a duplicate key error, which the
service converts into `409 Conflict` (`NFR8`). No lock or transaction is needed.

```kotlin
try {
    collection.insertMany(documents)
} catch (e: MongoBulkWriteException) {
    if (e.isDuplicateKey()) throw ConcurrentOrderModificationException(orderId)
    throw e
}
```

Order views are replaced as a whole with an upsert, and they are filtered by restaurant, customer and status. Indexes
on these fields, declared by the service itself, keep the lists fast:

| Collection     | Index                             | Purpose                                    |
|----------------|-----------------------------------|--------------------------------------------|
| `order_events` | `(aggregateId, sequence)`, unique | Replay, and detection of concurrent writes |
| `order_views`  | `(restaurantId, status)`          | Orders of a restaurant                     |
| `order_views`  | `(customerId, status)`            | Orders of a customer                       |
| `order_views`  | `(status)`                        | Filtering by status alone                  |

## Authentication

Users are authenticated with **JWT**, chosen over server-side sessions because any gateway replica can verify a token
on its own, with no shared store (`NFR1`). OAuth was not needed, as Munchies has no third-party identity provider and
no external application acting on behalf of users.

1. `user-service` checks the credentials, comparing the salted SHA-256 hash of the password with the stored one.
2. The gateway signs a token with HS256, holding the user id and role, and a 7 day expiry.
3. The token is set in the `authToken` cookie, which the browser sends with each following request.
4. On each protected route, the gateway verifies the signature and the expiry, and stores the user id and role on the
   request.

```ts
const payload: TokenClaims = {
    sub: id.value,
    [ID_CLAIM]: id.value,
    [ROLE_CLAIM]: role.name,
    exp: expSeconds,
    iat: nowSeconds,
};
```

Components are not authenticated to each other: calls inside the cluster carry no credentials, and access to Kafka and
MongoDB requires none.

## Authorization

Authorization is **RBAC** at the gateway, with an ownership check added by `restaurant-service`, which RBAC alone could
not express.

Each API contract declares its minimum role through `getRequiredAuthRole()`. When the gateway builds its routes, it adds
the checks of the matching role, so that a route cannot be exposed without them. Roles carry a numeric visibility, so
the check is a comparison, and a manager passes any check that a customer passes.

```ts
if (req.user?.role.visibility >= requiredRole.visibility) next();
```

The gateway copies the user id from the verified token into the forwarded request through `addId`, overriding anything
the client may have sent. `restaurant-service` then compares it with the `managerId` of the restaurant.
