# Automated Traffic System

Production-style Spring Boot backend for traffic ingestion, analytics, reporting, and AI-assisted insights.

## Highlights

- Layered architecture (`controller -> service -> repository`) with focused responsibilities.
- API versioning (`/api/v1`, `/api/v2`) with custom request condition support.
- Flyway-managed schema migrations (`V1__init_traffic_data.sql`) instead of runtime table mutation.
- Environment-specific profiles:
  - `dev` (default): in-memory H2 + H2 console + sample seed data.
  - `prod`: external DB via environment variables.
- Centralized error handling with structured error payloads (`status`, `errorCode`, `path`, `requestId`, `details`).
- Request correlation IDs (`X-Request-Id`) propagated to logs and responses.
- Operational visibility:
  - Spring Boot Actuator health/metrics endpoints.
  - Prometheus metrics endpoint.
- CI-ready setup:
  - GitHub Actions workflow for tests and JaCoCo coverage report artifact.
- Container-ready setup:
  - Multi-stage `Dockerfile`.
  - `docker-compose.yml` with app + PostgreSQL.

## Tech Stack

- Java 21
- Spring Boot 3.2
- Spring Data JPA
- Spring Validation
- Spring AI (OpenAI)
- Flyway
- H2 (dev), PostgreSQL (prod)
- OpenAPI / Swagger UI
- JUnit 5 + Mockito + JaCoCo

## Quick Start

### 1. Run locally (dev profile)

```bash
./gradlew bootRun
```

Application starts on `http://localhost:8080`.

### 2. Run tests

```bash
./gradlew clean test jacocoTestReport
```

Coverage report:

- `build/reports/jacoco/test/html/index.html`

### 3. Run with Docker Compose (prod-like)

```bash
docker compose up --build
```

This starts:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

## Configuration

### Dev profile (default)

- `src/main/resources/application-dev.properties`
- H2 console: `http://localhost:8080/h2-console`

### Prod profile

Set environment variables:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_DRIVER` (optional, defaults to PostgreSQL driver)
- `OPENAI_API_KEY` (optional; AI analysis degrades gracefully when missing)

## API Surface

Base routes:

- `/api/v1/traffic` - ingestion + retrieval + core analytics
- `/api/v2/traffic/stats` - extended statistics
- `/api/ai/traffic/*` - AI analysis/prediction
- `/api/reports` - text/json reporting

OpenAPI:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Observability

- Liveness/readiness health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus scrape: `/actuator/prometheus`

Each response includes `X-Request-Id`. The same ID is present in logs for request tracing.

## Project Structure

```text
src/main/java/com/example/automatedtrafficsystem
  |- controller/
  |- service/
  |- repository/
  |- model/
  |- exception/
  |- config/
  |- ai/
src/main/resources
  |- application.properties
  |- application-dev.properties
  |- application-prod.properties
  |- db/migration/
```

## Notes for Portfolio Review

This project intentionally demonstrates backend engineering concerns beyond CRUD:

- fail-fast input validation and deterministic error contracts
- profile-driven runtime behavior
- schema migration discipline
- request observability and operational endpoints
- CI, test coverage artifacts, and containerization
