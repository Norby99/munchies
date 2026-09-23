# Munchies — Frontend UI Specification

Source of truth for building a Vue 3 client against the current backend. Reflects the system **as implemented**, not as intended — gaps and inconsistencies are called out explicitly so they aren't accidentally designed around as if they were real features.

## 1. System architecture

```
Browser (Vue app)
   │  HTTPS, cookies
   ▼
gateway-service (Express/TS, port 8086 external / 8080 internal)
   │  proxies to, no shared auth forwarded — gateway resolves identity itself
   ├── user-service        (Micronaut/Kotlin, Mongo)
   ├── restaurant-service   (Micronaut/Kotlin, Mongo)
   ├── order-service        (Micronaut/Kotlin, Mongo)
   └── scheduler-service    (Express/TS, in-memory) ──▶ order-service + restaurant-service
```

- **Single base URL**, no version prefix (`/users/...`, `/restaurants/...`, `/orders/...` directly — not `/api/v1/...`).
- **user, restaurant, order and scheduler are reachable from the frontend.** `scheduler-service` is routed through the gateway under `/schedule/*` (see §9). `payment-service`, `notification-service` and `table-reservation-service` exist in the repo but are not routed through the gateway — do not design screens that call them directly.
- No CORS configuration and no rate limiting exist on the gateway today; if the Vue app is served from a different origin, this will need to be added on the backend first.

## 2. Auth model

- Session is an **httpOnly cookie** named `authToken` (JWT, HS256), set by the gateway on successful `login` or `register`. The Vue app never reads or stores the token itself — just needs `credentials: 'include'` (or equivalent) on every request.
- Cookie attributes: `Secure`, `SameSite=Lax`, `maxAge = 1h`. The JWT payload itself is valid for 7 days, but the browser will drop the cookie after 1 hour — **build silent-session-expiry handling into the app** (a 401/expired request should bounce to login, not surface as a generic error).
- Two roles, hierarchical: `CUSTOMER` (visibility 1), `MANAGER` (visibility 2 — satisfies customer-gated routes too). There is no `ADMIN` role and no staff/kitchen role.
- **No logout endpoint exists.** "Log out" in the UI must clear client state and can at best call a non-existent endpoint — flag this to backend, or implement logout as "clear cookie client-side" (only works if the cookie isn't httpOnly-enforced against JS clearing — confirm with backend; the honest interim UX is "close browser" or a backend fix).
- **No password-reset-via-email flow.** Only email *verification* (post-registration OTK) exists. Don't design a "Forgot password" link that has anywhere to go yet.
- Register requires a **client-computed password hash** (`SHA-256(password + salt)`, salt = 16 random bytes hex-encoded) sent as `hashedPassword` + `saltValue`. Login, by contrast, sends the **plaintext** password. The Vue app needs a small crypto util for the register form only.

## 3. Cross-cutting conventions

- **Success envelope:** `{ "result": <payload>, "code": <httpStatus> }`
- **Error envelope:** `{ "result": "<message>", "code": <httpStatus> }` — same shape, so a single response interceptor can branch on `code`.
- **Money** is always a **decimal string** (e.g. `"5.80"`), never a float — format/parse as `Intl.NumberFormat`, never `Number()` arithmetic before display.
- **Timestamps** (`estimatedDeliveryTime`, `pickupTime`) are **epoch-milliseconds encoded as strings** (e.g. `"1743500000000"`), not ISO dates. Parse with `new Date(Number(value))`.
- IDs are UUID strings everywhere.

---

## 4. User domain

### 4.1 Entity

```ts
User {
  id: string
  username: string
  email: string
  role: "CUSTOMER" | "MANAGER"
}
```
`email.isVerified` and all credential/lockout state exist server-side but are **never returned** in any response — the UI cannot show a "verified" badge or lockout status from data alone; it can only infer lockout from a specific error code during login/password-change.

### 4.2 Endpoints (via gateway, all cookie-authenticated except register/login)

| Action | Method & path | Body | Notes |
|---|---|---|---|
| Register | `POST /users/register/` | `{ user: {username, email, role}, hashedPassword, saltValue }` | Sets session cookie on success (auto-login). |
| Login | `POST /users/login/` | `{ email? , username?, password }` | Sets session cookie. `email` or `username`, plaintext password. |
| Get own profile | `GET /users/` | — | id comes from the session, not a param. |
| Update profile | `PATCH /users/update-info/` | `{ user: {username, email, role} }` | id injected server-side. Role is technically editable via this payload — lock it in the UI. |
| Change password | `PATCH /users/update-password/` | `{ email?, username?, oldHashedPassword /* actually plaintext */, newPassword }` | Field name is misleading — send the current password in plaintext. |
| Verify email | `GET /users/verify-email/` | `{ otk }` | GET-with-body; id injected server-side. |
| Delete account | `DELETE /users/` | — | Deletes own account, no confirmation mechanism server-side. |

### 4.3 Error/result states to design for

- Login: wrong credentials, account not found, **account locked** (see below).
- Register: email/username already registered.
- Change password: wrong current password, locked account, mismatched identity.
- **Lockout rule:** one wrong "current password" attempt during a password-change locks the account for **1 hour**, immediately (no multi-strike grace). Copy should warn about this before the user submits, not just after.

### 4.4 Screens implied

1. **Register** — username, email, password (+ confirm), role fixed to `CUSTOMER` for self-serve signup.
2. **Login** — email-or-username, password.
3. **Verify email** — single OTK input, reachable post-registration.
4. **Profile (view/edit)** — username, email; no avatar/address/preferences fields exist in the domain, don't invent them.
5. **Change password** — current + new password, with lockout warning copy.
6. **Delete account** — confirmation dialog is a pure frontend concern (backend takes no confirmation token).

No admin/user-management UI is possible — there is no list/search/ban/role-change endpoint.

---

## 5. Restaurant domain

### 5.1 Entities

```ts
Restaurant { id, name, address, phone, email }        // managerId owns it but is not exposed in the DTO

Menu { id, name, categories: Category[], validity: Validity }
MenuSummary { id, name }                                // list view — no categories/validity

Category { id, name, items: MenuItem[], variations: Variation[] }

MenuItem { id, name, description, price: string, variations: Variation[] }
// MenuItem has no validity field exposed via API even though the domain model supports one — don't build a per-item schedule UI yet.

Variation { name, options: VariationOption[] }
VariationOption { name, additionalPrice: string }
```

`Variation` is a modifier group (e.g. "Dough" → "Normal" / "Gluten-free", each with a price delta). It's attached at category level (applies to all items in it) and separately at item level. Build one reusable "variation group editor" component and reuse it in both places.

### 5.2 Validity — the availability rule engine

Menus (not individual items) carry a `Validity`, one of:

| Type | Shape | Meaning |
|---|---|---|
| `always` | `{}` | Default — no restriction |
| `period` | `{ start, end }` (ISO dates, inclusive) | Available within a date range |
| `yearly` | `{ startMonth, startDay, endMonth, endDay }` | Recurring yearly window (supports wraparound, e.g. Dec→Feb) |
| `weekly` | `{ days: number[] }` (ISO 1=Mon..7=Sun) | Available only on listed weekdays |
| `hours` | `{ start, end }` (`HH:MM[:SS]`) | Daily time window (supports overnight wraparound) |
| `from` / `until` | `{ start }` / `{ end }` | Open-ended range |
| `and` | `{ first, second }` (recursive) | Compound rule, e.g. weekly AND hours |

**The server never evaluates this** — no endpoint filters "available now." If the UI wants an "open now" indicator on a menu, it must replicate the `isValid(now)` logic client-side from the returned rule. Build this as one shared utility, since it's the only scheduling concept in the whole system (there's no restaurant-level opening-hours field at all — only menus have a schedule).

### 5.3 Endpoints (via gateway)

| Action | Method & path | Role | Notes |
|---|---|---|---|
| Create restaurant | `POST /restaurants/` | MANAGER | managerId injected server-side |
| Get restaurant | `GET /restaurants/:restaurantId/` | CUSTOMER | Single restaurant by known id only |
| List my restaurants | `GET /restaurants/manager/:managerId/` | MANAGER | ⚠️ Gateway route currently mismatches the backend controller (see §7) — verify against a live server before wiring |
| Update restaurant | `PUT /restaurants/:restaurantId/` | MANAGER | Owner-only (401 otherwise) |
| Delete restaurant | `DELETE /restaurants/:restaurantId/` | MANAGER | ⚠️ Gateway currently doesn't send the required body — verify before wiring |
| Create menu | `POST /restaurant/:restaurantId/menus` | MANAGER | Note: singular `/restaurant/...` for menu routes vs plural `/restaurants/...` for restaurant routes — this is a real inconsistency in the API, not a typo here |
| Get menu | `GET /restaurant/:restaurantId/menus/:menuId` | CUSTOMER | Full menu with nested categories/items |
| List menus | `GET /restaurant/:restaurantId/menus` | CUSTOMER | Returns `MenuSummary[]` only — id+name, no items |
| Update menu | `PUT /restaurant/:restaurantId/menus/:menuId` | MANAGER | Full replace of name + validity |
| Delete menu | `DELETE /restaurant/:restaurantId/menus/:menuId` | MANAGER | |
| Create category | `POST /restaurant/:restaurantId/menus/:menuId/categories` | MANAGER | |
| Update category | `PUT .../categories/:categoryId` | MANAGER | Full replace of name + variations |
| Delete category | `DELETE .../categories/:categoryId` | MANAGER | |
| Create item | `POST .../categories/:categoryId/items` | MANAGER | |
| Update item | `PUT .../categories/:categoryId/items/:itemId` | MANAGER | Full replace |
| Delete item | `DELETE .../categories/:categoryId/items/:itemId` | MANAGER | |

There is no `GET` for a single category or single item — both are only retrievable as part of the full menu fetch.

### 5.4 Validation to mirror client-side

| Field | Rule |
|---|---|
| Restaurant name / address | required, ≤255 / ≤500 chars |
| Phone | required, ≤20 chars, `^[+]?[0-9\s\-()]+$` |
| Email | required, ≤255 chars, standard email pattern |
| Menu name | required, ≤50 chars |
| Category name | required, ≤100 chars |
| Item name | required, ≤150 chars |
| Item description | required, ≤500 chars |
| Variation name | required, ≤100 chars |
| Price / additional price | ≥ 0 |
| Restaurant name uniqueness | unique **per manager**, not global |

### 5.5 Gaps to flag before scoping screens

- **No restaurant discovery for customers.** There is no list/search/browse-all-restaurants endpoint — only fetch-by-known-id or a manager's own list. A customer-facing "browse restaurants near me" screen has nothing to call yet; raise this before committing to that screen in the design.
- No open/closed status, no item "out of stock" flag, no approval/moderation state, no images anywhere in the domain — don't design badges or fields for these.
- No reordering (categories/items have no position field) and no bulk operations (no duplicate-menu, no bulk item import).

### 5.6 Screens implied

1. **Manager: My Restaurants** — list, create, edit, delete.
2. **Manager: Menu Builder** — per restaurant: menus → categories → items → variations, each level full-CRUD with the validity editor on menus.
3. **Customer: Restaurant Detail** — single restaurant (reached via a direct link/QR/id for now, since there's no browse screen) showing its menus/categories/items with variation pickers.

---

## 6. Order domain

### 6.1 Entity

Sealed by `orderType`, unified on the wire:

```ts
Order {
  orderId: string
  restaurantId: string
  customerId: string
  status: "PENDING" | "PREPARING" | "READY" | "ON_THE_WAY" | "COMPLETED" | "CANCELLED"
  items: { menuItemId: string, quantity: number }[]
  orderType: "DELIVERY" | "TAKEAWAY" | "DINE_IN"

  // DELIVERY only
  estimatedDeliveryTime?: string   // epoch ms as string
  deliveryAddress?: string
  bellName?: string
  customerPhone?: string

  // TAKEAWAY only
  pickupTime?: string              // epoch ms as string
  customerName?: string

  // DINE_IN only
  tableNumber?: number
  numberOfGuests?: number
}
```

**No price, name, or total is stored on order items or the order itself.** The order domain only knows `menuItemId` + `quantity`. Any cart subtotal, line-item price, or order total shown in the UI must be computed client-side by joining against the restaurant's menu data fetched separately (§5). Fetch and cache the relevant menu before building the order review screen.

**No cart exists server-side.** "Add to cart," quantity adjustments, and the running order draft are pure frontend state (e.g. a Pinia store) until the user checks out and calls `place` once with the full item list.

### 6.2 Endpoints (via gateway, all CUSTOMER, all cookie-authenticated)

| Action | Method & path | Body | Notes |
|---|---|---|---|
| Get order | `GET /orders/:id` | — | ⚠️ No ownership check server-side — any logged-in customer who knows the id can view it. Don't put raw order ids in shareable/guessable URLs. |
| Place order | `POST /orders/place` | Full order payload per type (see below) | `customerId` injected server-side |
| Advance status | `POST /orders/advance` | `{ orderId }` | Moves one step forward in the type's chain. ⚠️ Also has no ownership check. |
| Cancel order | `DELETE /orders/:id` | — | Only works while `status === PENDING`. **Deletes the record** — a cancelled order will 404 on subsequent `GET`, it will not show as `CANCELLED`. Design order history accordingly (see §6.4). |
| Update items | `PATCH /orders/items` | `{ orderId, items }` | Full replace of item list. Ownership-checked (401 if not yours). |
| Update delivery info | `PATCH /orders/delivery` | `{ orderId, estimatedDeliveryTime, deliveryAddress, bellName, customerPhone }` | Delivery orders only |
| Update takeaway info | `PATCH /orders/takeaway` | `{ orderId, pickupTime, customerName }` | Takeaway orders only |

There is **no update for dine-in table/guest info** after creation, and **no list/history endpoint** — a customer cannot fetch "my past orders" from this API today. Any order-history screen needs the frontend to track order ids itself (e.g. store placed order ids client-side) or needs a backend addition — flag this rather than designing around a call that doesn't exist.

### 6.3 Place-order payload by type

```ts
// DELIVERY
{ orderType: "DELIVERY", restaurantId, items, estimatedDeliveryTime, deliveryAddress, bellName, customerPhone }
// TAKEAWAY
{ orderType: "TAKEAWAY", restaurantId, items, pickupTime, customerName }
// DINE_IN
{ orderType: "DINE_IN", restaurantId, items, tableNumber, numberOfGuests }
```
`estimatedDeliveryTime` / `pickupTime` must be strictly in the future (validated server-side, returns `400` otherwise). `items` must be non-empty with every `quantity > 0`.

### 6.4 Status state machine

```
DELIVERY:          PENDING → PREPARING → READY → ON_THE_WAY → COMPLETED
TAKEAWAY / DINE_IN: PENDING → PREPARING → READY → COMPLETED

Cancel: PENDING → CANCELLED, only from PENDING, only customer-initiated,
        record is deleted rather than kept in a CANCELLED state.
```
Design the order-tracking screen as a stepper matching the type's chain (delivery gets 4 steps, the other two get 3). "Cancel" should only be enabled while `status === PENDING`. After a successful cancel, treat the order as gone (404 on refetch) rather than showing a "Cancelled" badge sourced from the server.

### 6.5 Screens implied

1. **Checkout / Place Order** — cart review (client-computed totals) → delivery/takeaway/dine-in details form (conditional fields per type) → submit.
2. **Order Tracking** — single order by id, status stepper per §6.4, edit items/delivery/takeaway while still editable, cancel button gated on `PENDING`.
3. **Order history** — not buildable against the current API; needs either a new backend endpoint or client-side tracking of placed order ids as an interim measure. Flag explicitly rather than silently omitting.

---

## 7. Known backend inconsistencies to verify before wiring (don't silently design around them)

1. Restaurant routes are plural (`/restaurants/...`); menu/category/item routes are singular (`/restaurant/...`). Real inconsistency, not a typo — the Vue API client should hardcode both forms as given, not "fix" the mismatch client-side.
2. Gateway's "list my restaurants" route builds `GET /restaurants/manager/:managerId/`, but the backend controller expects `GET /restaurants` with the manager id in the body — these do not currently match. Test live before finalizing the API client method.
3. Gateway's "delete restaurant" doesn't send the body the backend controller requires. Test live.
4. `GET /orders/:id` and `DELETE /orders/:id` have no ownership check — treat order ids as sensitive, don't expose them in shareable links.
5. Session cookie expires in 1h; the underlying JWT is valid 7 days — expect users to get silently logged out mid-session sooner than "remember me" language would imply.

---

## 8. Suggested Vue 3 application structure

```
src/
  api/
    client.ts            # axios/fetch wrapper, withCredentials, response envelope unwrap, 401 → redirect to /login
    users.ts              # register, login, getProfile, updateProfile, changePassword, verifyEmail, deleteAccount
    restaurants.ts         # CRUD + menu/category/item CRUD
    orders.ts               # place, get, advance, cancel, updateItems, updateDelivery, updateTakeaway
  stores/                    # Pinia
    auth.ts                   # session state, role, login/logout actions
    cart.ts                     # client-only draft order (items, orderType, type-specific fields) until place() is called
    restaurantCatalog.ts          # cached restaurant + menu data, exposes computed "isMenuAvailableNow(validity)"
  utils/
    validity.ts                    # client-side evaluator mirroring backend Validity.isValid(now)
    money.ts                        # decimal-string formatting/parsing (never float arithmetic)
    passwordHash.ts                  # SHA-256(password+salt) for registration only
  views/
    auth/{Login,Register,VerifyEmail}.vue
    account/{Profile,ChangePassword,DeleteAccount}.vue
    restaurant/{RestaurantDetail}.vue                 # customer-facing, id-based (no browse view — flag with product)
    manager/{MyRestaurants,MenuBuilder}.vue
    order/{Checkout,OrderTracking}.vue
  router/
    index.ts   # role-gated routes: manager/* requires role===MANAGER, everything else requires an active session except /login and /register
```

Route guards should redirect to `/login` on any `401`/`code: 401` response from the API interceptor, and gate `/manager/*` on `auth.role === 'MANAGER'` client-side (the backend enforces it regardless, but the UI shouldn't offer entry points a customer role will just get rejected from).

---

## 9. Scheduling domain (scheduler-service)

Standalone Express service behind the gateway under `/schedule/*`. It computes bookable time slots from a restaurant's capacity config and its own booking ledger, then places the chosen order in `order-service`. A background timer moves each order to `PREPARING` when its prep lead time is reached.

### 9.1 Entities (wire shapes)

```ts
AvailableRange { start: string /* "HH:MM" */, end: string, startEpochMs: number, endEpochMs: number, remaining: number }

ScheduleOrderResult { bookingId: string, orderId: string, restaurantId: string, orderType: OrderType, slotStartEpochMs: number, prepareAtEpochMs: number, status: "CONFIRMED" }
```

Unlike the rest of the platform, scheduler timestamps are **epoch-millisecond numbers**, not strings. `orderType` is `"DELIVERY" | "TAKEAWAY" | "DINE_IN"` as in the order domain.

### 9.2 Endpoints (via gateway)

| Action | Method & path | Role | Notes |
|---|---|---|---|
| Get bookable hours | `GET /schedule/:restaurantId/availability?date=YYYY-MM-DD&orderType=` | CUSTOMER | `date` required, `orderType` optional. Returns `{ result: { slotMinutes, maxOrdersPerSlot, ranges: AvailableRange[] } }`. Empty `ranges` = nothing bookable that day. |
| Kitchen load | `GET /schedule/:restaurantId/load?date=YYYY-MM-DD` | MANAGER | occupancy vs capacity per slot |
| Schedule + place order | `POST /schedule/:restaurantId/orders` | CUSTOMER | body `{ items:[{menuItemId,quantity}], orderType, slotStartEpochMs, deliveryAddress?, bellName?, customerPhone?, customerName?, tableNumber?, numberOfGuests? }`. `customerId` injected from the session. `201` + `ScheduleOrderResult`. `409` if the slot filled up between availability and submit — re-fetch availability and let the user re-pick. |
| Advance a scheduled order | `POST /schedule/orders/:orderId/advance` | MANAGER | forwards to `order-service` `advance` and reflects it on the booking |

### 9.3 Screens implied

1. **Slot picker** — after cart review, call availability for the target date, render `ranges` as selectable chips (dim/omit `remaining === 0`), submit the chosen `slotStartEpochMs` to `POST /schedule/:restaurantId/orders` instead of calling `order-service` `place` directly.
2. **Manager: kitchen load** — `load` per day as a capacity bar per slot.

### 9.4 Gaps to flag

- The capacity/output config (`slotMinutes`, `maxOrdersPerSlot`, opening/closing, prep lead, max-advance) is meant to live in `restaurant-service` (`GET /restaurants/:id/fulfillment-policy`) but that endpoint is **not implemented yet** — the scheduler uses a fixed default for every restaurant. No manager UI to edit it exists.
- Booking ledger is in-memory: a scheduler restart forgets confirmed bookings (the orders themselves survive in `order-service`).
- Only the first transition (→ `PREPARING`) is automated; `READY` onward is still a manual `advance`.
- `DINE_IN` orders carry no time in `order-service`, so a scheduled dine-in slot is only recorded on the booking.
