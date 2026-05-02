---
id: module-reference
title: Module Reference
sidebar_position: 1
---

# Module Reference

Quick reference for each Gradle module: where to find it, what it owns, what it depends on.

## `model`

`it.schwarz.coupon.model.*` — the wire format and BSON storage representation of a coupon.

- `rest/CouponDto.kt` — REST DTO. KDoc includes the canonical insertion JSON:

  ```json
  {
    "code": "abc123",
    "discount": 12.54,
    "description": "best coupon ev4",
    "applicationCount": 0
  }
  ```

- `rest/CouponListDto.kt` — paginated list wrapper.
- `rest/ErrorDto.kt` + `mapper/ErrorDto` extensions — uniform error envelope.
- `mongodb/CouponDocument.kt` — BSON-mapped document; converted via `mapper/CouponMapper.kt`.
- `serialization/` — `couponSerializersModule` with contextual serializers for
  `ObjectId`, `BigDecimal`, `Instant`.

**Depends on:** `kotlinx-serialization-json`, `mongodb-bson`, `mongodb-bson-kotlinx`.

## `configuration`

`it.schwarz.coupon.configuration.Database` — produces a `MongoClient` with the codec
registry needed for `kotlinx.serialization` and Java time. Provides Java test fixtures
(`mongodb-driver-kotlin-coroutine` + Testcontainers) so any module can spin up a real
Mongo for integration tests.

## `db-migrations`

Stand-alone executable Kotlin module (Gradle `application` plugin).

- `MainKt` — reads `MONGODB_URI` / `MONGODB_DATABASE` env vars and calls
  `MongoMigrations.run(...)`. This is the entry point for the `db-migrations.jar`
  produced for Compose.
- `MongoMigrations.kt` — singleton with two helpers:
    - `runMigrations()` — picks up config from system properties / env and is safe to call
      from app startup.
    - `run(uri, dbName)` — explicit invocation used by tests and `MainKt`.
- Migration registry tracks applied IDs in the `schema_migrations` collection. Two
  migrations are currently shipped:
    - `001-create-coupons-code-index` — unique index on `coupons.code`.
    - `002-create-coupons-ttl-index` — TTL index on `coupons.creationDateTime` (3 minutes).

See [Database Migrations](./database-migrations) for the full contract.

## `service`

`it.schwarz.coupon.service.*` — Ktor REST API on port 8082.

- `Application.kt` — entry point; calls `configureMigrations()` then `configureService()`.
- `configuration/ApiServiceRegistry.kt` — wires Koin modules, content negotiation,
  request validation, status pages, default headers, the `TraceIdHeaderPlugin` and
  routing.
- `repository/CouponRepository.kt` — MongoDB-coroutine-driver-backed persistence.
- `service/CouponService.kt` — domain service; throws `TooManyCodesException` for
  oversized bulk requests.
- `rest/couponRoutes.kt` — `POST /coupons`, `POST /coupons/bulk`, `GET /coupons`.
- `validation/validateCouponDto.kt` — installed into the `RequestValidation` plugin.

### DTO validation

`POST /coupons` validates the body via `validateCouponDto()` registered in the Ktor
`RequestValidation` plugin. Failures are mapped to `ErrorDto` with HTTP 400 by
`StatusPages`:

```kotlin
exception<RequestValidationException> { call, cause ->
    call.respond(
        status = HttpStatusCode.BadRequest,
        message = cause.toErrorDto(fallbackMessage = "Validation failed: ${cause.reasons.joinToString()}"),
    )
}
```

### Example request / response

```bash
curl -i -X POST http://localhost:8082/coupons \
  -H 'Content-Type: application/json' \
  -d '{ "code": "abc123", "discount": 12.54, "description": "best coupon ev4", "applicationCount": 0 }'
```

Response headers will include `X-Trace-Id: <hex>` — see
[Tracing & Observability](./tracing-observability) for what to do with it.

## `cleanup`

`it.schwarz.coupon.cleanup.*` — Ktor application that boots, runs a one-shot
`CleanupRunnerJob` via `runBlocking` on `ServerReady`, and exits. In Compose the
`cleanup-scheduler` (Ofelia) `docker exec`s the container on a cron schedule.

## `frontend`

Vue 3 + Vite + TypeScript. Talks to the `service` API on port 8082 and surfaces returned
trace IDs.

## `documentation` (this site)

Docusaurus 3.7 driven by the `com.github.node-gradle.node` Gradle plugin. The Gradle
build:

- runs `docusaurus gen-api-docs all` to materialise MDX from
  `service/src/main/resources/openapi/documentation.yaml`,
- runs `docusaurus build --out-dir build/site`,
- caches on inputs (`docs/`, `src/`, `static/`, `docusaurus.config.js`,
  `sidebars.js`, the OpenAPI spec).
