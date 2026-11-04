# Security

- Is there any form of __authentication__?
    * _where_? _why_?

- Is there any form of __authorization__?
    * which sort of _access control_?
    * which sorts of users / _roles_? which _access rights_?

- Are __cryptographic schemas__ being used?
    * e.g. token verification,
    * e.g. data encryption, etc.

---

# Security of Munchies

## Trust boundaries

The gateway is the boundary of the system: it treats whatever arrives from a client as untrusted, while services trust
what they receive from the gateway, and handle no tokens. This centralizes the security logic in one place, and it
relies on internal services being unreachable from outside (`NFR12`).

## Authentication

| Who                | Where             | How                                                                                           | Why                                                                                        |
|--------------------|-------------------|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Users, on login    | `user-service`    | Email or username, and password, checked against the stored hash                              | Credentials stay inside the context that owns them                                         |
| Users, on requests | `gateway-service` | A signed token in a cookie, verified on each protected request with no call to `user-service` | Sessions keep working if `user-service` is down, and replicas need no shared session store |
| Email ownership    | `user-service`    | A key sent to the email address                                                               | Proving that the address belongs to the user (`FR-A3`)                                     |
| Service to service | None              | Calls inside the cluster are not authenticated                                                | The network is assumed to be trusted; see the limits below                                 |

Tokens make sessions stateless, which matters for scaling (`NFR1`), but it makes revocation harder: a token stays valid
until it expires unless a shared revocation list is checked. Their lifetime is limited to 7 days (`NFR14`).

Brute-force attacks on a single account are limited by locking it after 3 consecutive failures (`FR-A4`).

## Authorization

Access control is **role-based** at the gateway, and **ownership-based** inside services.

### Roles

Each route of the API contract declares the minimum role it requires, and the gateway enforces it before forwarding.
Roles are ordered, so a manager can do everything a customer can.

| Role       | Access rights                                                                                                |
|------------|--------------------------------------------------------------------------------------------------------------|
| Anonymous  | Register, log in                                                                                             |
| `CUSTOMER` | Own account, including email verification; read restaurants and menus; place, change, cancel and read orders |
| `MANAGER`  | All of the above; create and manage restaurants, menus, categories and items                                 |

| Status             | Meaning                                            |
|--------------------|----------------------------------------------------|
| `401 Unauthorized` | The token is missing or invalid                    |
| `403 Forbidden`    | The token is valid, but the role is not sufficient |

Since the contract is shared with the service, the required role is declared in a single place, next to the path.

### Ownership

A role alone cannot express "only the manager of this restaurant". The gateway therefore passes the caller's id, taken
from the verified token and never from the request body, and `restaurant-service` compares it with the owner of the
restaurant.

## Cryptographic schemas

| Use            | Schema                                      | Purpose                                                           |
|----------------|---------------------------------------------|-------------------------------------------------------------------|
| Session tokens | JWT signed with HMAC                        | Integrity: a client cannot change its id or role without the key  |
| Passwords      | Salted hash, with a random salt per user    | No clear password is stored, and equal passwords hash differently |
| Transport      | HTTPS between client and system (_planned_) | Confidentiality of credentials and tokens in transit              |

The cookie holding the token has these attributes:

| Attribute       | Effect                                                   |
|-----------------|----------------------------------------------------------|
| `httpOnly`      | Scripts cannot read it, which limits the impact of XSS   |
| `secure`        | It is only sent over HTTPS                               |
| `sameSite: lax` | It is not sent on cross-site requests, which limits CSRF |

Tokens are not encrypted, since they only hold the user id and role, and no sensitive data. Logs never contain the
payloads of requests, which may hold credentials or personal data (`NFR13`).
