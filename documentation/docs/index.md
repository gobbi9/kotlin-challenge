---
id: index
title: coupon-kotlin coding challenge
sidebar_label: Overview
slug: /
---

# coupon-kotlin

A Kotlin/Ktor migration of an internal Java coupon service, structured as a Gradle multi-module
project with first-class observability (OpenTelemetry + Jaeger), MongoDB persistence with
schema migrations, and a Vue 3 frontend.

## What's in this site

| Section                                                | Purpose                                                           |
|--------------------------------------------------------|-------------------------------------------------------------------|
| [About](./about/application-architecture)              | Project tour, screenshots, module map and architecture decisions. |
| [Getting Started](./getting-started/local-environment) | Bring the stack up locally with Docker Compose or IntelliJ.       |
| [Development Guide](./development/module-reference)    | Module reference, tracing, OpenAPI, database migrations.          |
| [API Documentation](./api-overview)                    | Interactive REST reference generated from the OpenAPI spec.       |

## Stack at a glance

- **Language / runtime:** Kotlin 2.3, JDK 25 (Zulu, via `.sdkmanrc`).
- **Web framework:** [Ktor](https://ktor.io) 3.4 with Netty.
- **Persistence:** MongoDB 8 with the Kotlin coroutine driver.
- **DI:** Koin 4.
- **Tracing:** OpenTelemetry → Jaeger (OTLP gRPC on `:4317`).
- **Frontend:** Vue 3 + Vite + TypeScript.
- **Tooling:** ktlint, SonarQube, JaCoCo, Bruno (API testing).

## Quick start

```bash
sdk env install                            # JDK 25 (Zulu) and tooling
docker compose up --build -d               # MongoDB, Jaeger, services and frontend
./gradlew :documentation:docusaurusStart   # this site, with hot reload
```

See [Local Environment](./getting-started/local-environment) for the full walk-through.
