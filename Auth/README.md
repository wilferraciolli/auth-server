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

### 3. Complete first-time Keycloak setup

See the [Keycloak First-Time Setup](#keycloak-first-time-setup) section below for the full walkthrough — admin account reset, realm verification, service account role assignment, and client secret generation.

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

## Keycloak First-Time Setup

After running `docker compose up -d` for the first time, complete the following steps manually in the Admin Console at http://localhost:8080.

### 1. Reset the admin account

Keycloak 26 prompts you to update the temporary admin credentials on first login. If you skipped this or need to start fresh:

1. Log in with the credentials from `.env` (`KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`)
2. Go to **master** realm → **Users**
3. Delete the existing `admin` user
4. Go to **master** realm → **Users** → **Add user**
   - Set a username (e.g. `admin`)
   - Enable the user
5. Go to the new user → **Credentials** → **Set password** (mark as not temporary)
6. Go to the new user → **Role mapping** → **Assign role** → filter by `realm-management` → assign `admin`

### 2. Verify the homelab realm was imported

1. In the top-left realm dropdown, switch from **master** → **homelab**
2. If `homelab` is not listed, import it manually:
   - Go to the realm dropdown → **Create realm**
   - Click **Browse** and upload `keycloak/realm-import.json`
   - Click **Create**

### 3. Assign service account roles to `auth-server-client`

The realm import creates the client but does **not** automatically assign the service account roles. Do this once after first boot:

1. In the **homelab** realm, go to **Clients** → `auth-server-client`
2. Click the **Service accounts roles** tab
3. Click **Assign role**
4. In the filter dropdown, change **Filter by realm roles** → **Filter by clients**
5. Search for `realm-management`
6. Select `manage-users`, `view-users`, and `query-users`
7. Click **Assign**

### 4. Generate the client secret

1. Still on `auth-server-client`, go to the **Credentials** tab
2. Click **Regenerate** (dismiss the adapter warning toast — it is informational only)
3. Copy the new secret value
4. Paste it into your `.env` file:

```env
KEYCLOAK_CLIENT_SECRET=<your-secret-here>
```

---

## Keycloak Realm Reference

The `keycloak/realm-import.json` file pre-configures:

| Setting | Value |
|---|---|
| Realm | `homelab` |
| Client ID | `auth-server-client` |
| Client type | Confidential (service account) |
| Service account roles | `manage-users`, `view-users`, `query-users` from `realm-management` |

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
