# Auth Server

A Spring Boot 4 application that manages users in a Keycloak `homelab` realm via the Keycloak Admin REST API. Exposes a clean REST API for creating, reading, updating, deleting, and resetting passwords for users.

---

## Tech Stack

| Dependency | Purpose |
|---|---|
| Spring Boot 4 / Java 25 | Application framework |
| Spring Web MVC | REST API |
| Spring Data JPA + H2 | JPA satisfied with in-memory DB (user data lives in Keycloak) |
| Spring Validation | Request body validation (`@Valid`) |
| Keycloak Admin Client 26.6.2 | Strongly-typed Java client for the Keycloak Admin REST API |
| Lombok | Boilerplate reduction |
| SpringDoc OpenAPI 3 | Auto-generated Swagger UI |

---

## Configuration

All sensitive values are read from the root `.env` file (excluded from git). Spring Boot imports it automatically via `spring.config.import`.

**`application.yml` environment variables:**

| Variable | Default | Description |
|---|---|---|
| `KEYCLOAK_SERVER_URL` | `http://localhost:8080` | Keycloak server base URL |
| `KEYCLOAK_REALM` | `homelab` | Realm that owns the users |
| `KEYCLOAK_CLIENT_ID` | `auth-server-client` | Service account client ID |
| `KEYCLOAK_CLIENT_SECRET` | *(required)* | Client secret from Keycloak Admin UI |

Copy the root `.env` to each development machine. The app will fail to start if `KEYCLOAK_CLIENT_SECRET` is missing.

---

## Running

```bash
# From the auth-server/ directory
./mvnw spring-boot:run
```

The app starts on **port 8081** (Keycloak occupies 8080).

Swagger UI: http://localhost:8081/swagger-ui.html

---

## Package Structure

```
com.wiltech.auth_server/
├── config/
│   └── KeycloakAdminConfig.java   # Creates the Keycloak admin client bean
└── users/
    ├── UserDto.java               # Single DTO for requests and responses
    ├── PasswordDto.java           # DTO for the password reset endpoint
    ├── UserRepository.java        # Data layer — wraps the Keycloak Admin Client
    ├── UserService.java           # Business logic, 404 handling, partial updates
    └── UserRestService.java       # REST controller
```

The project follows **package-by-feature**: every class related to user management lives in the `users` package. Keycloak-specific types (`UserRepresentation`, `CredentialRepresentation`) are contained within `UserRepository` and never leak into higher layers.

---

## REST API

Base URL: `http://localhost:8081`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users?page=0&size=20` | List users (paginated) |
| `GET` | `/api/users/{id}` | Get user by Keycloak ID |
| `POST` | `/api/users` | Create user |
| `PUT` | `/api/users/{id}` | Partial update (only non-null fields applied) |
| `DELETE` | `/api/users/{id}` | Delete user |
| `PUT` | `/api/users/{id}/password` | Set or reset password |

### Create user — request body

```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "Welcome123!",
  "temporaryPassword": false
}
```

- `password` and `temporaryPassword` are **write-only** — never returned in responses
- `POST /api/users` returns `201 Created` with a `Location` header containing the new user's URL

### Update user — request body

Only the fields you include are updated. Omitted fields are left unchanged.

```json
{
  "firstName": "Jonathan",
  "email": "jonathan.doe@example.com"
}
```

### Set password — request body

```json
{
  "password": "NewPassword456!",
  "temporary": false
}
```

Set `"temporary": true` to force the user to change their password on next login.

---

## Testing

HTTP request samples are in [src/test/resources/users.http](src/test/resources/users.http).

Open the file in VS Code (REST Client extension) or IntelliJ. Update the `@userId` variable at the top with a real Keycloak user ID after your first create request — the ID is returned in the `Location` response header.

---

## Architecture Notes

- **Authentication flow:** The app uses the OAuth2 **Client Credentials** grant. It authenticates as itself (client ID + secret) against Keycloak — no user session is involved. Keycloak issues an access token that the admin client uses for all Admin API calls.
- **No local user storage:** Users are stored entirely in Keycloak. H2 is present only to satisfy the JPA dependency at startup — it holds no application data.
- **Partial updates:** `PUT /api/users/{id}` behaves like a `PATCH` — only non-null fields in the request body overwrite the existing user's values in Keycloak.
