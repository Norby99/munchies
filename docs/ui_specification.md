# Munchies — Frontend UI Specification

Reference for the Vue 3 client (`frontend-service`) and the backend it talks to. It describes the system **as
implemented**: remaining gaps and inconsistencies are called out explicitly, so they are not designed around as if
they were real features. How the frontend code is organized is described in `frontend-service/CLAUDE.md`.

## 1. System architecture

```
Browser (Vue SPA, served by nginx in frontend-service)
   │  same origin: REST over HTTPS + one WebSocket, session cookie
   ▼
gateway-service (Express/TS, port 8086 external / 8080 internal)
   │  authenticates the cookie, injects the caller's identity, proxies
   ├── user-service               (Micronaut/Kotlin, Mongo)
   ├── restaurant-service         (Micronaut/Kotlin, Mongo)        menus, tables, scheduling settings
   ├── order-service              (Micronaut/Kotlin, Mongo)        event-sourced Order aggregate
   ├── payment-service            (Express/TS, Mongo)
   ├── table-reservation-service  (Express/TS, Mongo)
   ├── scheduler-service          (Express/TS, in-memory) ──▶ order-service + restaurant-service
   └── notification-service       (Express/TS, Kafka consumer) ◀── Kafka ◀── order / payment / reservation / user
          ▲ WebSocket (Socket.io) proxied by the gateway: live notifications to the browser
```

- **Single base URL**, no version prefix (`/users/...`, `/restaurants/...`, `/orders/...`). The frontend's nginx
  (and the Vite dev server) proxies these prefixes to the gateway, so the SPA and the API share one origin.
- Every backend service the UI needs is routed through the gateway; the frontend never calls a service directly.
- No CORS configuration and no rate limiting on the gateway: the same-origin setup makes CORS unnecessary.

## 2. Auth model

- Session is an **httpOnly cookie** named `authToken` (JWT, HS256), set by the gateway on successful `register`
  (auto-login) or `login`.
  `Secure`, `SameSite=Lax`, `maxAge` 7 days, aligned with the JWT expiry. The SPA never reads or stores the token;
  it sends credentials on every request (`withCredentials`).
- The gateway injects the caller's id (and, where relevant, `customerId` / `managerId`) from the cookie: request
  bodies still carry those fields because the shared DTOs declare them, but they are sent empty and ignored.
- Two roles, hierarchical: `CUSTOMER`, `MANAGER` (a manager also satisfies customer-gated routes). No admin role.
- **Logout** is `POST /users/logout/`, handled by the gateway, which clears the httpOnly cookie.
- A `401` anywhere means "session expired": the client clears its session state and returns to login.
- **No password reset by email.** Only email verification (one-time key sent after registration) exists.
- Register sends a **client-computed password hash** (`SHA-256(password + salt)`, salt = 16 random bytes,
  hex) as `hashedPassword` + `saltValue`; login sends the plaintext password.

## 3. Cross-cutting conventions

- **Envelope:** success `{ "result": <payload>, "code": <httpStatus> }`, error `{ "result": "<message>", "code" }`
  — same shape, so one interceptor unwraps it and branches on `code` (`api/client.ts`).
- **Menu money** is a **decimal string** (e.g. `"5.80"`). **Order money** is an integer number of **euro cents**
  (`unitPriceCents`, `totalCents`), frozen by order-service when the items are ordered.
- **Time:** order times (`estimatedDeliveryTime`, `pickupTime`) are epoch milliseconds as **strings**; scheduler
  times are epoch milliseconds as **numbers** plus restaurant-local `"HH:MM"`; reservation `start` / `end` are
  ISO-8601 UTC instants.
- IDs are UUID strings everywhere.
- A `404` on a list endpoint (orders, unset scheduling settings) means "nothing there", not an error.

---

## 4. User domain

### 4.1 Entity

```ts
User { id, username, email, role: "CUSTOMER" | "MANAGER", isEmailVerified: boolean }
```

### 4.2 Endpoints

| Action | Method & path | Body | Notes |
|---|---|---|---|
| Register | `POST /users/register/` | `{ username, email, role, hashedPassword, saltValue }` | Sets the session cookie; the email is verified afterwards. |
| Login | `POST /users/login/` | `{ email? , username?, password }` | Sets the session cookie. |
| Logout | `POST /users/logout/` | `{ userId: "" }` | Gateway clears the cookie. |
| Verify email | `POST /users/verify-email/` | `{ id: "", otk }` | |
| Get own profile | `GET /users/` | — | id from the session. |
| Update profile | `PATCH /users/update-info/` | `{ user: { id: "", username, email, role } }` | Role is technically editable: the UI locks it. |
| Change password | `PATCH /users/update-password/` | `{ email?, username?, oldHashedPassword, newPassword }` | `oldHashedPassword` is actually the current password in plaintext. |
| Delete account | `DELETE /users/` | — | Confirmation is a UI concern. |

**Lockout rule:** one wrong current password during a password change locks the account for **1 hour**. The
change-password form warns about it before submitting.

### 4.3 Screens

Login, Register, Verify email (bare layout, no app shell); Account (profile, password, delete).

---

## 5. Restaurant domain

### 5.1 Entities

```ts
Restaurant { id, name, address, phone, email }      // managerId owns it but is not exposed
Menu { id, name, categories: Category[], validity: Validity[] }
MenuSummary { id, name }
Category { id, name, items: MenuItem[], variations: Variation[] }
MenuItem { id, name, description, price: string, variations: Variation[] }
Variation { name, options: { name, additionalPrice: string }[] }
DiningTable { number, seats }                        // number unique within the restaurant
CapacityPolicy { slotMinutes, maxCapacityUnits, opening, closing, prepLeadMinutes, maxAdvanceDays,
                 maxTripsPerSlot, maxTripKmBudget, maxDetourKm }
GeoLocation { lat, lng }
```

`Variation` is a modifier group (e.g. "Dough" → "Normal" / "Gluten-free", each with a price delta), attached at
category level (applies to all its items) and at item level.

### 5.2 Validity — menu availability rules

A menu's `validity` is a **list** of rules that must all hold (an empty list or `always` means no restriction):

| Type | Shape | Meaning |
|---|---|---|
| `always` | `{}` | No restriction |
| `period` | `{ start, end }` (ISO dates, inclusive) | Date range |
| `yearly` | `{ startMonth, startDay, endMonth, endDay }` | Recurring yearly window (wraps, e.g. Dec→Feb) |
| `weekly` | `{ days: number[] }` (ISO 1=Mon..7=Sun) | Listed weekdays |
| `hours` | `{ start, end }` (`HH:MM[:SS]`) | Daily window (wraps overnight) |
| `from` / `until` | `{ start }` / `{ end }` | Open-ended range |

On the wire each rule is a flat `ValidityDto { type, value: Record<string,string> }`; `api/menus.ts` decodes it.
**The server never evaluates validity**: "available now" is computed client-side (`utils/validity.ts`).

### 5.3 Endpoints

| Action | Method & path | Role | Notes |
|---|---|---|---|
| Search restaurants | `GET /restaurants/?q=` | CUSTOMER | Name or address, case-insensitive; empty `q` lists all. Capped server-side. |
| Get restaurant | `GET /restaurants/:restaurantId/` | CUSTOMER | |
| List my restaurants | `GET /restaurants/manager/` | MANAGER | GET with an empty JSON body; manager id from the session. |
| Create / update / delete | `POST /restaurants/`, `PUT` / `DELETE /restaurants/:restaurantId/` | MANAGER | Owner-only. |
| Get / set tables | `GET` / `PUT /restaurants/:restaurantId/tables/` | CUSTOMER / MANAGER | `PUT { tables: DiningTable[] }` replaces the layout; duplicate numbers → `422`. |
| Get / set capacity policy | `GET` / `PUT /restaurants/:restaurantId/capacity-policy/` | CUSTOMER / MANAGER | `404` = not configured. |
| Get / set location | `GET` / `PUT /restaurants/:restaurantId/location/` | MANAGER | `404` = not configured. Delivery needs both policy and location. |
| Menus | `GET` / `POST /restaurant/:restaurantId/menus`, `GET` / `PUT` / `DELETE .../menus/:menuId` | read CUSTOMER, write MANAGER | Note the singular `/restaurant/` (see §11). `PUT` replaces name + validity. |
| Categories | `POST .../menus/:menuId/categories`, `PUT` / `DELETE .../categories/:categoryId` | MANAGER | `PUT` replaces name + variations. |
| Items | `POST .../categories/:categoryId/items`, `PUT` / `DELETE .../items/:itemId` | MANAGER | `PUT` replaces the whole item. |

There is no `GET` for a single category or item: both come with the full menu.

### 5.4 Validation mirrored client-side

| Field | Rule |
|---|---|
| Restaurant name / address | required, ≤255 / ≤500 chars |
| Phone | required, ≤20 chars, `^[+]?[0-9\s\-()]+$` |
| Email | required, ≤255 chars, standard email pattern |
| Menu / category / item name | required, ≤50 / ≤100 / ≤150 chars |
| Item description | required, ≤500 chars |
| Variation name | required, ≤100 chars |
| Price / additional price | ≥ 0 |
| Restaurant name uniqueness | unique **per manager** |

### 5.5 Gaps

- No images, open/closed flag, "out of stock" flag or moderation state anywhere in the domain.
- No ordering of categories/items and no bulk operations (duplicate menu, import).

### 5.6 Screens

- **Customer:** Explore (search), Restaurant detail (menus, variation picker, order draft, table booking).
- **Manager:** My restaurants (CRUD, scheduling settings, tables), Menu builder (menus → categories → items →
  variations, validity editor on menus).

---

## 6. Order domain

### 6.1 Entity

```ts
Order {
  orderId, restaurantId, customerId: string
  status: "PENDING" | "PREPARING" | "READY" | "ON_THE_WAY" | "COMPLETED" | "CANCELLED"
  orderType: "DELIVERY" | "TAKEAWAY" | "DINE_IN"
  items: { menuItemId: string, quantity: number, unitPriceCents: number }[]
  payed: boolean                 // spelled like this on the wire
  totalCents: number
  // DELIVERY: estimatedDeliveryTime (epoch ms string), deliveryAddress, bellName, customerPhone
  // TAKEAWAY: pickupTime (epoch ms string), customerName
  // DINE_IN:  tableNumber, numberOfGuests
}
```

Prices are **frozen by order-service** from the menu when items are ordered, so totals stay right if the menu
changes later; the client never sends prices. The cart is pure frontend state until checkout. The `Order` aggregate
is **event-sourced**: nothing is deleted, a cancelled order stays readable with status `CANCELLED`.

### 6.2 Endpoints

| Action | Method & path | Body | Notes |
|---|---|---|---|
| List orders | `GET /orders/?restaurantId=&status=` | — | A customer always gets **only their own** (gateway forces `customerId`); a manager filters by restaurant. `404` = empty. |
| Get order | `GET /orders/:id` | — | A customer only gets their own: the gateway forwards their id and anyone else's order is a `404`. Managers are not filtered (see §11). |
| Place order | `POST /orders/place` | per type, see below | Used directly only for **dine-in**; delivery and takeaway go through the scheduler (§9). |
| Advance status | `POST /orders/advance` | `{ orderId }` | Managers advance scheduled orders through `/schedule/orders/:id/advance` instead (§9). |
| Cancel order | `DELETE /orders/:id` | — | Only the caller's own order (`404` otherwise), only while `PENDING` and **unpaid**: a paid order is refused with `400` (no refunds). Enforced by order-service. |
| Update items | `PATCH /orders/items` | `{ orderId, customerId: "", items }` | Full replace; owner-only. |
| Update delivery / takeaway info | `PATCH /orders/delivery`, `PATCH /orders/takeaway` | type-specific fields | |

Dine-in payload: `{ orderType: "DINE_IN", restaurantId, items, reservationId, tableNumber, numberOfGuests }` — the
order must reference one of the customer's active reservations at that restaurant; table and guests are taken from
the reservation.

### 6.3 Status chains

```
DELIVERY:           PENDING → PREPARING → READY → ON_THE_WAY → COMPLETED
TAKEAWAY / DINE_IN: PENDING → PREPARING → READY → COMPLETED
Cancel:             PENDING (unpaid) → CANCELLED, by the customer who owns the order
```

### 6.4 Screens

Checkout (type choice, details, slot or table), My orders (list), Order detail (stepper per chain, lines and total,
pay, cancel), Manager order board (columns per status, advance, kitchen load).

---

## 7. Payment domain

| Action | Method & path | Body | Notes |
|---|---|---|---|
| Pay an order | `POST /payments/` | `{ orderId, customerId: "", paymentDetails: { method } }` | `method` is `CARD` or `MOBILE_PAYMENT` in the UI. payment-service checks the order is the caller's and unpaid, and charges `totalCents` as priced by order-service. |

Result: `{ id, orderId, status, amount (cents), currency }`. Payment is simulated (no real gateway).

---

## 8. Table reservation domain

```ts
Reservation { id, restaurantId, customerId, start, end /* ISO UTC */, numberOfGuests, tableNumber,
              status: "ACCEPTED" | "CANCELED" }
```

Rules (enforced by table-reservation-service): arrival on a quarter of an hour and in the future; fixed length of
**120 minutes**; one table per party, the smallest free one that fits. A restaurant is bookable only after its
manager configures tables (§5.3).

| Action | Method & path | Notes |
|---|---|---|
| Book | `POST /reservations/` `{ restaurantId, customerId: "", start, numberOfGuests }` | `409` = no free table. |
| List | `GET /reservations/?restaurantId=` | Customer: own reservations (optionally at one restaurant). Manager: one of their restaurants (`restaurantId` required). Sorted by start. |
| Get one | `GET /reservations/:id/` | A customer only gets their own. |
| Change | `PUT /reservations/:id/` `{ customerId: "", start, numberOfGuests }` | Until it starts; may move to another table. |
| Cancel | `PATCH /reservations/:id/cancel/` `{ customerId: "" }` | Until it starts; stays visible as `CANCELED`. |

Screens: booking dialog on the restaurant page, My reservations (customer), Reservations by day (manager,
read-only), Tables dialog (manager).

---

## 9. Scheduling domain (scheduler-service)

Sits between the customer and order-service for **delivery and takeaway**: it offers the time slots the kitchen can
still take, books one, and only then places the order. A background timer moves each order to `PREPARING` when its
prep lead time is reached.

```ts
AvailableSlot { start, end /* "HH:MM" local */, startEpochMs, endEpochMs, remaining /* kitchen units */ }
Availability { restaurantId, date, orderType, slotMinutes /* 0 = type not offered */, maxCapacityUnits,
               ranges: AvailableSlot[] }
```

| Action | Method & path | Role | Notes |
|---|---|---|---|
| Availability | `GET /schedule/:restaurantId/availability?date=YYYY-MM-DD&orderType=&lat=&lng=` | CUSTOMER | `lat`/`lng` required for `DELIVERY`: a slot is offered only if a delivery trip can still reach the address. |
| Kitchen load | `GET /schedule/:restaurantId/load?date=` | MANAGER | Units booked against the admission quota, per slot. |
| Schedule + place | `POST /schedule/:restaurantId/orders` | CUSTOMER | `{ items, orderType, slotStartEpochMs, ...type fields }`. `409` = slot just taken: re-fetch availability. |
| Advance | `POST /schedule/orders/:orderId/advance` | MANAGER | Advances in order-service and keeps the booking in step. |

Gaps:

- The **booking ledger is in memory**: a scheduler restart forgets bookings (orders survive in order-service).
- The per-slot **admission quota is random** for now (`RandomAdmissionQuotaAgent`), pending the real capacity agent.
- Only the first transition (→ `PREPARING`) is automated; `READY` onward is a manual advance.

---

## 10. Live notifications

One **WebSocket (Socket.io)** per signed-in browser at `/notifications`, opened after login and closed on logout.
The gateway authenticates the upgrade with the session cookie (`401` otherwise), strips any client-sent identity
headers, adds the user's id, role and (for a manager) owned restaurants, and proxies it to notification-service,
which pushes to the rooms `user:<id>` and `restaurant:<id>`. It is receive-only; actions still go through REST.

```ts
OrderChangedNotification       { type: "ORDER_CHANGED", orderId, restaurantId, customerId, status, payed }
ReservationChangedNotification { type: "RESERVATION_CHANGED", change: "ACCEPTED" | "MODIFIED" | "CANCELED",
                                 reservationId, restaurantId, customerId, start, end, numberOfGuests, tableNumber }
```

- Order changes (placed, paid, advanced, cancelled) go to the customer and the restaurant; reservation changes to
  both as well. Payment and email events are not pushed.
- Nothing is stored: notifications sent while a browser is offline are lost, so screens refetch after a
  reconnection and the REST API stays the source of truth. The header bell lists the current page session only.
- notification-service runs as a single replica (rooms are in memory).

UI: screens showing the affected data refresh; a toast (`aria-live="polite"`) is shown only for what the user did
not do themselves (customer: order progress; manager: new / paid / cancelled orders and bookings).

---

## 11. Known inconsistencies

1. Restaurant routes are plural (`/restaurants/...`), menu/category/item routes singular (`/restaurant/...`). Real
   inconsistency; the client uses both forms as they are.
2. **Managers are not scoped to their restaurants** when reading one order (`GET /orders/:id`), listing orders
   (`GET /orders/?restaurantId=`) or reading one reservation: a manager who knows the id of another restaurant's
   order can read it. Customers are scoped (the gateway forwards their id and services answer `404` for anything
   else). Closing it needs the gateway to check the restaurant against the manager's own list, an extra call
   per request.
3. **Managers cannot cancel or reject orders.** There is no such feature: cancelling is the customer's, and a
   restaurant-side rejection would need its own event, a check on the restaurant, and refunds, which do not exist.
4. Listing a manager's restaurants is a `GET` with a JSON body, as is the gateway's internal call.
5. `PATCH /users/update-password/` names the current plaintext password `oldHashedPassword`.
