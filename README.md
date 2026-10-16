# URL Shortener API

A REST API for shortening URLs, built as a portfolio project.

## Stack
- Java 21
- Spring Boot 3
- PostgreSQL 16
- Flyway (database migrations)
- Docker Compose (local infra)

## Features
- Shortens a URL into a short code generated with [Hashids](https://hashids.org/)
- Returns the same short code when the same URL is submitted again (no duplicate records)
- Redirects short codes to the original URL and counts every click
- Exposes click statistics for a short code
- Validates the submitted URL and returns structured JSON errors

## Getting Started

### Pre-requisites
- JDK 21
- Docker and Docker Compose

### 1. Create the environment file
Copy the example file and fill in the values:

```bash
cp .env.example .env
```

| Variable | Description | Example |
|---|---|---|
| `POSTGRES_DB` | Database name created by the Postgres container | `url_shortener` |
| `POSTGRES_DB_URL` | JDBC URL used by the application | `jdbc:postgresql://localhost:5432/url_shortener` |
| `POSTGRES_DB_USER` | Database user | `postgres` |
| `POSTGRES_DB_PASSWORD` | Database password | `change-me` |
| `HASHIDS_SALT` | Secret salt used to generate short codes. The example value is a weak development placeholder; in production use a long random secret. See [Security notes](#security-notes) | `dev-salt-change-me` |

The `.env` file is ignored by git and is loaded automatically by the application at startup.

### 2. Start the infrastructure
```bash
docker compose up -d
```

This starts PostgreSQL 16 on port `5432`.

### 3. Run the application
Via the command line:
```bash
./mvnw spring-boot:run
```

Or from your IDE, running the main class `UrlShortenerApplication`.

The API is available at `http://localhost:8080`. Flyway applies the database migrations on startup.

## Endpoints

### Shorten a URL
`POST /api/v1/short-url`

Request:
```json
{
  "url": "https://example.com/some-long-page"
}
```

Response (`201 Created`):
```json
{
  "shortCode": "DQm5Q0",
  "originalURL": "https://example.com/some-long-page"
}
```

`201 Created` when the URL is new, `200 OK` when it was already shortened (same body).

### Get statistics
`GET /api/v1/short-url/{code}/stats`

Response (`200 OK`):
```json
{
  "shortCode": "DQm5Q0",
  "originalURL": "https://example.com/some-long-page",
  "clickCount": 42,
  "createdAt": "2026-10-14T11:20:00"
}
```

### Redirect
`GET /{code}` redirects (`302 Found`) to the original URL.

## Error responses

Errors share the same JSON structure:

```json
{
  "timestamp": "2026-10-14T14:03:12.345Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Short URL with shortCode: DQm5Q0, not found!",
  "path": "/api/v1/short-url/DQm5Q0/stats"
}
```

| Status | When |
|---|---|
| `400 Bad Request` | Missing or malformed request body, or validation failed (missing or invalid `url`) |
| `404 Not Found` | The short code does not exist, or the path does not match any resource |
| `405 Method Not Allowed` | The HTTP method is not supported for the endpoint |
| `415 Unsupported Media Type` | The request content type is not supported |
| `500 Internal Server Error` | Unexpected error |

## Security notes

- **Salt.** The default salt (`dev-salt-local`) and the placeholder in `.env.example` are weak on purpose and exist only for development. In production, set a long, random `HASHIDS_SALT` kept as a secret, and do not reuse it across environments.
- **Short codes are obfuscated, not encrypted.** Hashids encodes sequential database ids, so codes can be enumerated, and decoded by anyone who knows the salt. This is an accepted trade-off, the same as in public URL shorteners. `CodeGenerator` is a port, so codes can be replaced with random ones without touching the rest of the application.
- **Statistics exposure.** The original URL is already public through the redirect (`GET /{code}`), so `GET /api/v1/short-url/{code}/stats` exposes nothing beyond `clickCount` and `createdAt`.
- **No rate limiting yet.** Requests are not throttled. Rate limiting is on the roadmap.
