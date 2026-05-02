---
id: local-environment
title: Local Environment
sidebar_position: 1
---

# Local Environment

This page walks you from a freshly-cloned repository to a running stack.

## Prerequisites

| Tool                                                              | Version                                 | Purpose                                                                                                                                    |
|-------------------------------------------------------------------|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| [SDKMAN!](https://sdkman.io)                                      | latest                                  | Manages the Java toolchain.                                                                                                                |
| JDK                                                               | **25.0.3-zulu** (pinned in `.sdkmanrc`) | Builds the Kotlin/Ktor modules.                                                                                                            |
| Node.js                                                           | **22.12+ LTS**                          | Runs Vite (frontend) and Docusaurus (this site). The `node-gradle` plugin will download Node 22.12 automatically for documentation builds. |
| Docker / Podman                                                   | recent                                  | Runs MongoDB, Jaeger and (optionally) the apps.                                                                                            |
| [Bruno](https://www.usebruno.com)                                 | latest                                  | Hand-curated REST collection (`coupon-api/`).                                                                                              |
| [MongoDB Compass](https://www.mongodb.com/products/tools/compass) | latest                                  | Optional: GUI inspection of the `coupon-db` database.                                                                                      |

## Tooling install

```bash
# 1. JDK pinned in .sdkmanrc
sdk env install

# 2. OpenTelemetry Java agent (referenced from .run/Service.run.xml and Cleanup.run.xml)
curl -L -o opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# 3. (Optional) Node 22.12+ — only needed for running the frontend or docs outside Gradle
nvm install 22.12
nvm use 22.12
```

:::tip
Gradle's JVM toolchain mechanism will provision the right JDK automatically once
`.sdkmanrc` is sourced, so you don't need to install JDK 25 globally.
:::

## Start the stack with Docker Compose

```bash
docker compose up --build -d
```

Services come up in the right order — the migrator runs to completion first, then the
service/cleanup containers start. See [Docker & Ports](./docker-and-ports) for the full
topology and a port table.

Once everything is up:

- Frontend: <http://localhost:8081>
- Service (REST + Swagger): <http://localhost:8082> · <http://localhost:8082/swagger>
- Cleanup (short-lived container, it will exit after is done): <http://localhost:8083>
- Jaeger UI: <http://localhost:16686>

## Run from IntelliJ

Run configurations are committed under `.run/`:

- **Service** — Ktor app, env file `service/local.env`, OpenTelemetry Java agent attached.
- **Cleanup** — Ktor app, env file `cleanup/local.env`, OpenTelemetry Java agent attached.
- **Frontend** — `npm run dev` against `frontend/package.json`.

See [IntelliJ Configuration](./intellij-configuration) for details, including the port
collision caveat with the Compose stack.

## API testing with Bruno

The `coupon-api/` folder is a [Bruno](https://www.usebruno.com) collection:

```text
coupon-api/
├── bruno.json
├── Get coupons.bru
├── Get coupons by codes.bru
└── Save coupons.bru
```

Open Bruno → "Open Collection" → point it at `coupon-api/`. The default base URL is
`http://localhost:8080/`; adjust it to `http://localhost:8082/` (or use Bruno
environments) to match this project.

A starter request body for `POST /coupons`:

```json
{
  "code": "abc123",
  "discount": 12.54,
  "description": "best coupon ev4",
  "applicationCount": 0
}
```

## Build the documentation

```bash
./gradlew :documentation:build              # produces build/site/
./gradlew :documentation:docusaurusStart    # dev server with hot reload (port 3000)
```

The Gradle task uses the `com.github.node-gradle.node` plugin, so a project-local Node 22.12
is downloaded into `documentation/build/.gradle/` on first run. Cache inputs are wired
to `docs/`, `src/`, `static/`, `docusaurus.config.js`, `sidebars.js` and the OpenAPI
spec — Gradle will skip the build if none of those have changed.

### CORS and Local API Testing

If you are running the documentation dev server (`localhost:3000`) and want to test the interactive API documentation
against a local service (`localhost:8082`), you need to enable CORS in the service.

In `service/local.env`, ensure:

- `ALLOW_CORS=true`
- `CORS_DOMAINS=localhost,kaufa-digits.github.io`

The `CORS_DOMAINS` variable is a comma-separated list of allowed domains. For `localhost`, all ports are automatically
allowed.
