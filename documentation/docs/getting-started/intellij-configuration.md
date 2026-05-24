---
id: intellij-configuration
title: IntelliJ Configuration
sidebar_position: 3
---

# IntelliJ Configuration

The repository ships with three pre-built IntelliJ run configurations under `.run/`:

| Run config   | Type             | Main / command                            | Notes                                                         |
|--------------|------------------|-------------------------------------------|---------------------------------------------------------------|
| **Service**  | Ktor application | `it.schwarz.coupon.service.ApplicationKt` | Loads `service/local.env`; OpenTelemetry Java agent attached. |
| **Cleanup**  | Ktor application | `it.schwarz.coupon.cleanup.ApplicationKt` | Loads `cleanup/local.env`; OpenTelemetry Java agent attached. |
| **Frontend** | npm script       | `npm run dev` on `frontend/package.json`  | Uses the project Node interpreter.                            |

Open IntelliJ → the run config dropdown in the toolbar exposes all three by name.

## VM args

Both Ktor configs pass:

```text
-javaagent:$PROJECT_DIR$/opentelemetry-javaagent.jar
```

The agent jar is automatically downloaded by `mise install` (see the
[Local Environment](./local-environment#tooling-install) page).

## Env files

`service/local.env` and `cleanup/local.env` are the source of truth for local config:
`MONGODB_URI`, `DATABASE_NAME`, `OTEL_EXPORTER_OTLP_ENDPOINT`, etc. They are loaded by
the IDE's "envFile" extension so the values appear in `System.getenv(...)` exactly as
they would in the container.

## Caveat: port collisions with Docker Compose

The IntelliJ run configurations bind the **same host ports** as the Compose containers
(`8082`, `8083`). Running both at once will fail with `Address already in use`.

Stop the relevant container before launching from the IDE:

```bash
docker compose stop coupon-service          # before running Service in IntelliJ
docker compose stop coupon-cleanup          # before running Cleanup in IntelliJ
```

The MongoDB and Jaeger containers should stay up — the IDE-launched apps use the same
`MONGODB_URI` / `OTEL_EXPORTER_OTLP_ENDPOINT` values, just resolved against `localhost`.

## Coverage and code style

- The two Ktor configs include coverage patterns (`it.schwarz.coupon.service.*`,
  `cleanup.*`) so IntelliJ's "Run with Coverage" works out of the box.
- Code style is enforced by ktlint (Gradle plugin `org.jlleitschuh.gradle.ktlint`) plus
  `.editorconfig`. IntelliJ honours `.editorconfig` automatically; ktlint can be
  surfaced inside the IDE via the official ktlint plugin if you want lint feedback in
  the editor.
