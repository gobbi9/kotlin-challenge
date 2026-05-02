---
id: database-migrations
title: Database Migrations
sidebar_position: 4
---

# Database Migrations

Schema management lives in its own Gradle module — `db-migrations` — packaged both as a
runnable jar **and** exposed as a callable function (`MongoMigrations.run(...)`). This
gives us two equivalent ways to apply migrations:

| Mode             | Used by                                 | How                                                                                                                                         |
|------------------|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| **Container**    | Docker Compose                          | `db-migrations` service runs `java -jar db-migrations.jar`; downstream apps wait on `service_completed_successfully`.                       |
| **Programmatic** | `service`, `cleanup`, integration tests | `MongoMigrations.runMigrations()` reads `MONGODB_URI` / `DATABASE_NAME` from sysprops/env and short-circuits on already-applied migrations. |

## Why a separate module?

- **Strict dependency in Compose.** No orchestration plugin is needed to make sure
  migrations run before the apps; Compose's `depends_on … condition:
  service_completed_successfully` does the job.
- **Idempotency by design.** Migration IDs are recorded in a dedicated
  `schema_migrations` collection before being considered applied. Both the container
  and the in-process call will skip migrations that are already there, so calling
  `runMigrations()` from app startup is safe even right after the migrator container
  ran.
- **One source of truth.** Migrations are Kotlin code, alongside the rest of the
  domain. There is no separate language or migration framework to keep in sync with
  Mongo driver versions.
- **Testability.** Integration tests boot Mongo with Testcontainers and call
  `MongoMigrations.run(uri, dbName)` directly — no shell-out, no jar packaging.

## Anatomy

`db-migrations/src/main/kotlin/it/schwarz/coupon/migrations/MongoMigrations.kt`:

```kotlin
object MongoMigrations {
    private const val MIGRATIONS_COLLECTION = "schema_migrations"

    private val migrations = listOf(
        Migration(
            id = "001-create-coupons-code-index",
            description = "Create unique index on coupons.code",
            action = { db ->
                db.getCollection<Document>("coupons").createIndex(
                    Indexes.ascending("code"),
                    IndexOptions().unique(true).name("idx_coupons_code_unique"),
                )
            },
        ),
        Migration(
            id = "002-create-coupons-ttl-index",
            description = "Create TTL index on coupons.creationDateTime",
            action = { db ->
                db.getCollection<Document>("coupons").createIndex(
                    Indexes.ascending("creationDateTime"),
                    IndexOptions().expireAfter(3L, TimeUnit.MINUTES).name("idx_coupons_creation_ttl"),
                )
            },
        ),
    )

    fun runMigrations() { /* env/syspropsbased entry point */ }
    fun run(mongodbUri: String, databaseName: String, databaseProvider: () -> Database = { Database() }) { /* ... */ }
}
```

- Migrations are sorted by `id` and applied in order.
- Each successful migration inserts a row into `schema_migrations` with its ID,
  description and `appliedAt` timestamp.
- A failed migration aborts the run (the exception is rethrown after logging).

## Adding a migration

1. Append a new `Migration(...)` to the `migrations` list with a monotonic, descriptive
   ID (e.g. `003-add-customer-id-index`).
2. Keep the `action` block idempotent in spirit — it will only run once per environment,
   but treat the action as if it could re-run (e.g. use `createIndex` not `dropIndex`
   followed by `createIndex`).
3. If you need a destructive step, add it as a second migration with a clear ID rather
   than rewriting an applied one.
4. Run the integration tests; they spin up a real Mongo via Testcontainers and exercise
   the migrator end-to-end.

## Running it

```bash
# Inside the Compose stack — runs automatically as part of `up`
docker compose up --build -d

# As a Gradle invocation (uses the application plugin)
./gradlew :db-migrations:run \
  -DMONGODB_URI=mongodb://admin:admin@localhost:27017/?authSource=admin&replicaSet=coupon-db-rs \
  -DDATABASE_NAME=coupon-db
```

## TTL behaviour

The 3-minute TTL on `creationDateTime` is intentionally aggressive — it makes the
cleanup behaviour observable in a local demo (insert a coupon, watch it disappear).
For production-like settings, override the index by adding a follow-up migration or
parameterise it via the `cleanup/local.env` configuration.
