# coupon-kotlin

Kotlin/Ktor coupon service. Full technical documentation can be accessed
here: <https://gobbi9.github.io/kotlin-challenge/> or in the `documentation/`
module and is rendered by Docusaurus.

## Local setup

```bash
# 1. Install the JDK pinned in mise.toml (currently Java zulu-25.34.17.0).
mise install

# 2. Download the OpenTelemetry Java agent referenced by the IntelliJ run configs.
curl -L -o opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
```

Node **22** is required for the frontend and documentation modules. The Gradle build
of the `documentation` module downloads its own Node 22 via the
`com.github.node-gradle.node` plugin, so a system-wide install is only needed if you
want to run npm directly.

## Build the documentation

```bash
./gradlew :documentation:build              # static site → documentation/build/site
./gradlew :documentation:docusaurusStart    # dev server with hot reload (port 3000)
```

The site covers architecture, local setup, IntelliJ run configs, tracing and the
interactive OpenAPI reference. Open `documentation/build/site/index.html` after a
build, or visit <http://localhost:3000> when running the dev server.
