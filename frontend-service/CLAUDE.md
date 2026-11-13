# frontend-service

Guidance specific to `frontend-service`. The root `CLAUDE.md` still applies (language, documentation, commit and
testing rules); this file only adds what is specific to the frontend.

## Role

`frontend-service` is the single user interface of Munchies: a Vue.js Single Page Application (SPA) used both by
customers and by restaurant staff. It holds no business logic of its own: every business rule lives in the backend
microservices, and the frontend only presents their data, collects user input, and gives immediate feedback.

It talks to **`gateway-service` only**. It never calls `user-service`, `restaurant-service`, `order-service`,
`payment-service` or any other backend service directly, and it never touches Kafka or MongoDB.

The project is also the exam project of the "Applicazioni e Servizi Web" course (MEVN stack), so the frontend must
satisfy the course requirements listed in [Course requirements](#course-requirements) in addition to the
functional ones.

## Users and features

The gateway knows two roles (`AuthRole` in `commons`): `CUSTOMER` and `MANAGER`. The UI must adapt navigation and
available actions to the logged-in role, and guard routes accordingly (the gateway enforces authorization anyway;
the frontend guards exist for UX, not for security).

Features currently backed by gateway routes
(`gateway-service/src/main/ts/infrastructure/adapter/middleware/routes/`):

- **Account (any user)**: register, verify email, login, logout, view profile, update personal info, change
  password, delete account.
- **Customer**:
    - browse restaurants and their menus, categories and menu items;
    - place an order (delivery or takeaway), edit its items or its delivery/takeaway details, discard it;
    - pay an order;
    - list own orders and follow the status of each one.
- **Manager (restaurant staff)**:
    - create, update and delete own restaurants;
    - manage menus, categories and menu items;
    - see incoming orders and advance their status through the order lifecycle.

Planned, not yet in the UI (design it so they can be added without restructuring):

- **Table reservations** (`table-reservation-service`): the backend exists and is routed by the gateway under
  `/reservations/*` (reserve, list, read, modify, cancel) and `/restaurants/{id}/tables/` (the manager's table
  layout), with contracts in `table-reservation-shared` and `restaurant-shared`; no screen uses it yet.
- **Delivery scheduling** (`scheduler-service`).
- **Real-time notifications**: `notification-service` will push notifications (e.g. order status changes, payment
  success) to the frontend over a WebSocket proxied by the gateway (see
  [Communication with the gateway](#communication-with-the-gateway)). Until it exists, refresh data on navigation/user action; do not build a
  polling loop that would later have to be torn out. See the `event-driven` skill for the backend side.

When a feature needs data the gateway does not expose yet, the fix belongs in `gateway-service` (and the owning
service), not in a workaround in the frontend.

## Tech stack

| Concern             | Choice                                                                       |
|---------------------|------------------------------------------------------------------------------|
| Framework           | Vue 3, Single-File Components (`.vue`)                                       |
| Component style     | Composition API with `<script setup lang="ts">` (no Options API, no mixins)  |
| Language            | TypeScript, strict mode                                                      |
| Build / dev server  | Vite (`npm run dev`, `npm run build` to `dist/`, `npm run preview`)          |
| Routing             | Vue Router (`createWebHistory`), named routes, lazy-loaded views             |
| Global state        | Pinia, setup-store syntax (`defineStore('id', () => { ... })`)               |
| HTTP client         | Axios, wrapped in a single API module, always sending cookies (no `fetch`)   |
| API contracts       | Kotlin Multiplatform `-shared` packages (paths, methods, DTOs, JSON mapping) |
| UI components       | shadcn-vue (components copied into `src/components/ui/`, built on Reka UI)   |
| Styling             | Tailwind CSS only (theme tokens as CSS variables); no SCSS, no Bootstrap     |
| Unit/component test | Vitest + Vue Test Utils (Vitest is already used by `gateway-service`)        |
| Packaging           | Docker image, see [Runtime and deployment](#runtime-and-deployment)          |

Do not add a different framework (React, Angular, Nuxt), a second state library, or a packaged Vue component library
(Vuetify, PrimeVue, Quasar, Element Plus, ...). UI components come from **shadcn-vue**, which is not a dependency
but a set of components whose source is copied into the project, so they stay ours to read, style and fix. See
[UI components (shadcn-vue)](#ui-components-shadcn-vue).

Note: shadcn/ui (ui.shadcn.com) is React-only. This project uses its Vue port, **shadcn-vue**
(www.shadcn-vue.com): use its docs, its CLI (`shadcn-vue`) and its `.vue` components, never React snippets
(`.tsx`, `className`, `asChild`, `lucide-react`).

## Project layout

Organize code by responsibility, keeping components small and focused:

```
src/
  main.ts            app bootstrap: createApp, Pinia, router, global styles
  App.vue            root layout (header / main / footer, <RouterView />)
  router/            route table, navigation guards (auth + role)
  views/             one component per route (page-level, may use stores and API)
  components/        reusable presentational components (props in, events out)
    ui/              shadcn-vue components, added with the CLI (one folder per component)
  lib/utils.ts       `cn()` helper used by shadcn-vue to merge Tailwind classes
  composables/       reusable stateful logic (useXxx functions)
  stores/            Pinia stores for state shared across views (session, cart, ...)
  api/               the only place that performs HTTP calls; one module per backend area
                     (user, restaurant, order, ...) plus a shared client
  types/             TypeScript types for API payloads and view models
  assets/index.css   Tailwind entry point and theme tokens (see "Styling")
scripts/shadcn-vue.sh  runs the shadcn-vue CLI in Docker (see "UI components")
```

Rules:

- **Components** use PascalCase file and tag names (`OrderCard.vue`, `<OrderCard />`), declare `defineProps<...>()`
  and `defineEmits<...>()` with TypeScript types, and never mutate props (one-way data flow: props down, events up).
- **Prefer props and events** for parent/child communication; use Pinia only for state that is genuinely shared
  across views (logged-in user, current order/cart). `provide`/`inject` is for configuration-like context (theme,
  locale), not a replacement for props or for a store.
- **Views and stores call the `api/` layer**, never Axios/`fetch` directly; components under `components/` should
  not call the API at all.
- Load data in `onMounted` or in route-level logic, and always model the loading, empty and error states in the UI.

## Communication with the gateway

The frontend has exactly two channels towards the backend, both terminated by `gateway-service`:

- **REST over HTTP (Axios)** for every request/response interaction: account, restaurants, menus, orders,
  payments, and any future feature (reservations, scheduling).
- **One WebSocket, only for receiving notifications.** The frontend opens it on the same origin (e.g.
  `wss://<origin>/notifications`); the gateway authenticates the upgrade request with the `authToken` cookie and
  proxies the connection to `notification-service` (which stays internal, with no exposed port), forwarding the
  user identity. Frames are relayed end to end: there is no REST step and no polling. The socket is read-only from
  the frontend's point of view; actions triggered by a notification go through the REST channel. Open it after
  login, close it on logout, and reconnect with backoff when it drops.

- All requests go to the gateway under a single base path, configured via environment (`GATEWAY_URL` in the
  container, a Vite env variable / dev proxy locally). Never hard-code service hosts or ports.
- Serve the SPA and proxy the API from the **same origin** (Vite dev-server proxy in development, the frontend
  container's web server in production). This keeps the auth cookie working with `SameSite=lax` and avoids CORS
  configuration on the gateway.
- **Authentication is cookie-based.** On login the gateway sets an `authToken` cookie that is `HttpOnly`, `Secure`,
  `SameSite=lax`, and it blanks the token in the response body. Consequences:
    - the frontend never reads, stores or forwards the JWT, and must never put it in `localStorage` /
      `sessionStorage` (XSS exposure);
    - every request must send credentials (`withCredentials: true` in Axios, `credentials: 'include'` with `fetch`);
    - keep only non-sensitive session info (user id, role) in the session store, taken from the login response or
      from the "get user" endpoint, and rebuild it on page reload by calling that endpoint;
    - treat a `401` from the gateway as "session expired": clear the session store and redirect to login.
- Errors from the gateway follow the shared `ErrorResponse` shape (`message`, `code`, from `munchies-commons`); map
  them to user-friendly messages in one place in the `api/` layer.

### Shared API contracts (`-shared` packages)

Like the other Express.js services (`gateway-service`, `payment-service`, `notification-service`), the frontend does
**not** hand-write endpoint paths or DTOs: it reuses the Kotlin Multiplatform `-shared` modules compiled to JS.

- **What they provide**: each `<service>-shared` module exposes, per endpoint, a JS API class (e.g. `LoginUserAPI`
  from `munchies-user-service-shared/kotlin/user-modules`, built on `API`/`SimpleAPI` in `commons`) with
  `getPath()`, `getMethod()`, request/response classes with `toJson()`, and `parseRequest()` / `parseResponse()` /
  `parseError()` for JSON mapping, plus the generated `.d.ts` typings. `munchies-commons/kotlin/commons-modules`
  provides `HttpMethod`, `AuthRole`, `ErrorResponse` and the other cross-service types.
- **Packages needed**: the ones the gateway routes use: `munchies-commons`, `munchies-user-service-shared`,
  `munchies-restaurant-service-shared`, `munchies-order-service-shared` (add `munchies-gateway-service-shared` or
  others only when a feature needs them).
- **How they get here** (same mechanism as `build-logic/.../express-server.gradle.kts`):
    1. declare them in `frontend-service/build.gradle.kts` as `jsImplementation(project(":commons"))`,
       `jsImplementation(project(":user-shared"))`, ...;
    2. a `moveJsDeps` task runs each module's `pack_<module>` task and copies the resulting tarball to
       `frontend-service/build/libs/munchies-<module>.tgz`. The `frontend-server` convention plugin does not have
       this yet: add the `jsImplementation` configuration and `moveJsDeps` to it, reusing the express-server logic
       rather than duplicating a different mechanism;
    3. reference them in `package.json` as `"munchies-user-service-shared": "file:./build/libs/munchies-user-shared.tgz"`;
    4. **remove the `integrity` field** of those entries from `package-lock.json` (the tarballs are rebuilt on every
       build, see root `CLAUDE.md`);
    5. since the image is compiled inside Docker (`frontend-service/Dockerfile`), the Docker build must copy
       `build/libs/*.tgz` into the build stage before `npm install`.
- **How to use them in `api/`**: Axios is the transport, the shared API class is the contract. Build the URL from
  `getPath()` (filling `{param}` placeholders), the verb from `getMethod()`, send `request.toJson()` as the body,
  and turn the raw response text into typed objects with `parseResponse()` / `parseError()`. For these calls,
  configure Axios to keep the raw body (`responseType: 'text'`, no JSON transform) so that the shared parser does
  the mapping, exactly as the gateway does.
- Keep the Kotlin-generated classes inside the `api/` layer: views and stores receive plain, frontend-friendly
  view models, so the rest of the app does not depend on Kotlin/JS specifics.

## UI components (shadcn-vue)

Components come from shadcn-vue. The project skill `.claude/skills/shadcn-vue` (installed with
`npx skills add unovue/shadcn-vue`, pinned in the root `skills-lock.json`) covers the CLI, composition rules, forms,
theming and the MCP server: follow it whenever adding or changing UI.

- **Run the CLI through Docker, never on the host**: `scripts/shadcn-vue.sh <args>` (e.g.
  `scripts/shadcn-vue.sh add button card`, `scripts/shadcn-vue.sh info --json`) runs `npx shadcn-vue@latest` in a
  throwaway `node` container on a copy of `frontend-service/`, and copies back only the files the CLI created or
  changed (`src/`, `package.json`, `package-lock.json`, `components.json`). The host `node_modules` is never
  touched; the Docker build installs new dependencies with `npm ci`. The project context the skill tries to inject
  with a host `npx` call is therefore empty: use the script's `info` instead.
- **Dependencies are changed in a container too**, lockfile only, e.g.
  `docker run --rm -u "$(id -u):$(id -g)" -e HOME=/tmp -v "$PWD":/app -w /app node:24.0.0-alpine npm install --package-lock-only <pkg>`.
- **Use an existing component before writing a custom one**, and compose them (e.g. a form is `Field`/`FieldGroup`
  with inputs; an option set is a `ToggleGroup`; a dialog always has a `DialogTitle`). Use the built-in variants
  (`variant`, `size`) before adding custom classes.
- **The copied components are project code**: they may be edited, but keep changes minimal and deliberate, and
  re-check them with `npx shadcn-vue@latest add <component> --diff` before updating. Do not edit them only to
  restyle a single use: style the use site, or add a variant.
- **Accessibility comes from Reka UI** (focus management, keyboard handling, ARIA on dialogs, menus, tabs, ...):
  do not replace those components with hand-written markup, and keep labels, titles and descriptions they require.
- Feature components stay in `components/` and views in `views/`; they *use* `components/ui/`, which contains only
  generic, shadcn-vue-generated building blocks.

## Styling

Styling uses **Tailwind CSS only**, as required by shadcn-vue. SCSS and Bootstrap are not used.

- **Theme tokens are CSS variables** (shadcn-vue theming: `--background`, `--foreground`, `--primary`,
  `--muted-foreground`, `--radius`, ...) defined in the Tailwind entry stylesheet, with a light and a dark theme.
  Change the look of the app by changing these tokens, not by overriding colors per component.
- **Use semantic utility classes** (`bg-primary`, `text-muted-foreground`, `border-border`), never raw palette
  values (`bg-blue-500`) or hard-coded colors; no manual `dark:` color overrides.
- **Layout with Tailwind utilities**: `flex`/`grid` with `gap-*` (not `space-x-*`/`space-y-*`), `size-*` for equal
  width and height, `cn()` for conditional classes instead of template string ternaries.
- **Mobile-first**: base classes target small screens; larger screens use the responsive prefixes (`md:`, `lg:`).
  If custom breakpoints are needed, define them once in the Tailwind theme.
- Avoid `<style>` blocks in SFCs; when one is truly needed (e.g. a complex animation), keep it scoped and use the
  theme variables.
- **Status colors** are theme tokens too (`success`, `warning`, `info`, besides `primary` and `destructive`), used
  as text on a light tint of themselves (`bg-success/12 text-success`); `OrderStatusBadge` maps order states to
  them. Every token pair is checked for WCAG AA contrast in both themes: keep it that way when retuning colors.
- **Dark mode**: the theme follows the system setting or the user's choice in the account menu
  (`composables/useTheme.ts`), applied as the `dark` class on `<html>`; an inline script in `index.html` applies it
  before the first paint.
- Icons come from `@lucide/vue` (the `iconLibrary` in `components.json`), imported with the `...Icon` suffix.

## Course requirements

The frontend is graded as part of the "Applicazioni e Servizi Web" exam, which requires:

- **User-Centered Design**: design starts from users (customer, restaurant staff), their goals and task flows;
  document personas, scenarios and the information architecture in the report.
- **Responsive design**: mobile-first layouts that work on phone, tablet and desktop; breakpoints defined once in
  the Tailwind theme.
- **Accessibility as a requirement**: target WCAG 2.2 AA. Use semantic HTML first, ARIA roles/attributes only where
  semantics are missing, visible focus, full keyboard navigation, labels on every form control, sufficient color
  contrast, text alternatives for images, and announce asynchronous updates (errors, notifications) with live
  regions.
- **CSS preprocessing/styling**: the course presents SCSS (and treats it as extra credit). This project deliberately
  uses Tailwind CSS with shadcn-vue instead (see [Styling](#styling)); the report must justify that choice
  (consistency and accessibility of a shared component base, theming through tokens) since SCSS will not be shown.
- **TypeScript** across the codebase.
- **Usability tests with real users** on the implemented UI; results and the changes they triggered go in the
  LaTeX report delivered with the repository.

When making UI decisions, record the reasoning (design and accessibility choices) so it can be reported and
discussed at the oral exam.

## Runtime and deployment

- The service is started by `config/service/docker-compose.yml` as `munchies-frontend-service`: built from this
  directory, it listens on port `8080` inside the container (mapped to `5173` on the host), reads `GATEWAY_URL` from
  the environment, depends on `gateway-service` being healthy, and must answer `GET /health` for its healthcheck.
- The production image serves the static `dist/` bundle produced by `vite build` and proxies API calls to
  `GATEWAY_URL`. Unknown paths must fall back to `index.html` so that history-mode routes survive a page reload.

## Testing

Follow the root testing pyramid, adapted to the frontend:

- **Unit tests** (Vitest): composables, stores, API-layer mapping, utility functions.
- **Component tests** (Vitest + Vue Test Utils): components and views rendered in isolation, with the `api/` layer
  mocked; cover loading/error/empty states and role-dependent rendering.
- **End-to-end**: system-wide behavior is covered by the Cucumber suite in `e2e-test/`; do not create a separate
  e2e framework here without discussing it first.
- Accessibility checks (e.g. automated axe checks in component tests) complement, but do not replace, manual
  keyboard/screen-reader testing and the usability tests with users.
