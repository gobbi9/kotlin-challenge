---
id: tracing-observability
title: Tracing & Observability
sidebar_position: 2
---

# Tracing & Observability

The two Ktor apps (`service`, `cleanup`) are instrumented end-to-end with
[OpenTelemetry](https://opentelemetry.io). Traces are exported via OTLP/gRPC to Jaeger
on `:4317` and visible at <http://localhost:16686>.

## OpenTelemetry agent

Both `Service` and `Cleanup` IntelliJ run configurations attach the OpenTelemetry Java
agent:

```text
-javaagent:$PROJECT_DIR$/opentelemetry-javaagent.jar
```

In containers the agent is bundled via the module's Dockerfile. The exporter target
(`OTEL_EXPORTER_OTLP_ENDPOINT`) is overridden to `http://jaeger:4317` in
`docker-compose.yml`, and falls back to whatever is in `service/local.env` /
`cleanup/local.env` when running outside Compose.

Library-level instrumentation that ships with the project:

| Dependency                                  | Purpose                                 |
|---------------------------------------------|-----------------------------------------|
| `opentelemetry-api` / `opentelemetry-sdk`   | core SDK                                |
| `opentelemetry-exporter-otlp`               | OTLP/gRPC exporter                      |
| `opentelemetry-mongo-3.1`                   | MongoDB driver instrumentation          |
| `opentelemetry-extension-kotlin`            | suspend-friendly `Context` propagation  |
| `opentelemetry-logback-appender-1.0`        | injects trace/span IDs into Logback MDC |
| `opentelemetry-instrumentation-annotations` | `@WithSpan` for ad-hoc spans            |

## Trace ID in the response — `TraceIdHeaderPlugin`

`service/src/main/kotlin/it/schwarz/coupon/service/configuration/ApiServiceRegistry.kt`
defines a Ktor application plugin that emits the active trace ID as the `X-Trace-Id`
response header:

```kotlin
val TraceIdHeaderPlugin = createApplicationPlugin(name = "TraceIdHeader") {
    onCall { call ->
        val traceId = Span.current().spanContext.traceId
        if (traceId != "00000000000000000000000000000000") {
            call.response.headers.append(name = "X-Trace-Id", value = traceId)
        }
    }
}

fun Application.configureTraceIdHeader() {
    install(TraceIdHeaderPlugin)
}
```

It is installed unconditionally from `configureService()`, so every response carries a
trace ID whenever a real OTel span is in scope. The plugin guards against the
all-zero "invalid" trace ID — which is what `Span.current().spanContext.traceId` returns
when no span exists — to avoid emitting a misleading header.

### Use it from the frontend / Bruno / curl

```bash
curl -i http://localhost:8082/coupons | grep -i x-trace-id
# X-Trace-Id: 5a3a0a4e1e7d9b1e0c3f4d5e6f7a8b9c

# Open in Jaeger:
open "http://localhost:16686/trace/5a3a0a4e1e7d9b1e0c3f4d5e6f7a8b9c"
```

The frontend logs the header to the browser console for the same purpose. In Bruno, the
trace ID shows up under the response headers panel.

## Logging — Logback pattern

All three JVM modules share the same Logback pattern (configured in each module's
`src/main/resources/logback.xml`):

```text
%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [trace_id=%X{trace_id}] [span_id=%X{span_id}] - %msg%n
```

The `%X{trace_id}` and `%X{span_id}` placeholders are populated by the OpenTelemetry
Logback appender, which injects the active trace/span IDs into Logback MDC. This makes
correlating a Jaeger trace with a log line a copy-paste:

```text
2025-04-29 10:42:11.108 [eventLoopGroupProxy-3-1] INFO  i.s.coupon.service.CouponService [trace_id=5a3a0a4e1e7d9b1e0c3f4d5e6f7a8b9c] [span_id=b1c2d3e4f5a6b7c8] - inserted coupon abc123
```

`it.schwarz.coupon` is set to `trace` level by default to surface domain logs in local
runs.

## Code quality

| Tool                                         | What it enforces                                                                                                                             | Where it's configured                                            |
|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| **ktlint** (`org.jlleitschuh.gradle.ktlint`) | Kotlin style, formatting                                                                                                                     | Root `build.gradle.kts` (`afterEvaluate`); reports as Checkstyle |
| **`.editorconfig`**                          | Indentation + select ktlint disables (`multiline-expression-wrapping`, `function-signature`, `class-signature`, `chain-method-continuation`) | Repo root                                                        |
| **SonarQube** (`org.sonarqube`)              | Static analysis, coverage exclusions                                                                                                         | Root `build.gradle.kts`                                          |
| **JaCoCo**                                   | Coverage XML/HTML reports                                                                                                                    | Root `subprojects { ... }` block                                 |

Run them locally:

```bash
./gradlew ktlintCheck            # style check
./gradlew ktlintFormat           # auto-fix
./gradlew test jacocoTestReport  # coverage
./gradlew sonar                  # if SONAR_TOKEN/host is configured
```
