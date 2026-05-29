# HomeLab Auth

A Spring Boot application that manages users via the Keycloak Admin REST API, backed by a self-hosted Keycloak 26.x instance running in Docker.

---

## Architecture

| Component | Description |
|---|---|
| **Keycloak 26.x** | Identity and Access Management server |
| **PostgreSQL 16** | Persistent database for Keycloak |
| **auth-server** | Spring Boot 4 app that calls the Keycloak Admin API to manage users |

---

## Getting Started

### Prerequisites

- Docker and Docker Compose installed
- Java 25 + Maven (for the Spring Boot app)

### 1. Configure credentials

Copy `.env.example` to `.env` and fill in your values (the `.env` file is excluded from git):

```env
POSTGRES_DB=keycloak
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=change-me

KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=change-me
```

### 2. Start Keycloak and PostgreSQL

```bash
docker compose up -d
```

Keycloak will start in **dev mode** (`start-dev`) and automatically import the `homelab` realm on first boot.

### 3. Verify Keycloak

- Admin Console: http://localhost:8080
- Log in with the credentials set in `.env`
- Confirm the `homelab` realm is present
- Under **homelab → Clients**, confirm `auth-server-client` exists with *Service Accounts Enabled*
- Under **auth-server-client → Service account roles**, confirm `manage-users` (from `realm-management`) is assigned

### 4. Run the Spring Boot app

```bash
cd auth-server
./mvnw spring-boot:run
```

---

## Project Structure

```
Auth/
├── docker-compose.yml          # Keycloak + PostgreSQL services
├── .env                        # Credentials (not committed)
├── .gitignore
├── keycloak/
│   └── realm-import.json       # Homelab realm + auth-server-client definition
└── auth-server/                # Spring Boot user management API
    └── src/
```

---

## Keycloak Realm Setup

The `keycloak/realm-import.json` file pre-configures:

| Setting | Value |
|---|---|
| Realm | `homelab` |
| Client ID | `auth-server-client` |
| Client type | Confidential (service account) |
| Service account role | `manage-users` from `realm-management` |

> **Note:** The client secret in the import JSON is a placeholder. Regenerate it from the Admin UI (*auth-server-client → Credentials → Regenerate*) after first start — the realm config is preserved.

---

## Ports

| Service | Port | Notes |
|---|---|---|
| Keycloak | `8080` | Admin Console and token endpoint |
| Spring Boot (auth-server) | `8081` | User management REST API (configure in `application.properties`) |
| PostgreSQL | `5432` | Internal to Docker network; not exposed by default |

---

## Useful Commands

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f keycloak

# Stop all services
docker compose down

# Stop and remove volumes (wipe data)
docker compose down -v
```

---

## Notes

- Keycloak runs in **dev mode** — suitable for homelab/development. For production, switch to `start` mode and configure TLS.
- The `master` realm is intentionally left untouched. All application configuration lives in the `homelab` realm.
- `manage-users` grants the service account permission to create/read/update/delete users. If the Spring Boot app later needs to assign roles or manage clients, update the service account roles in the Admin Console.
