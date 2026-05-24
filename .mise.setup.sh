#!/bin/bash
# This script is sourced by mise whenever the environment is loaded.
# It ensures that the OpenTelemetry Java agent is downloaded if missing.

if [ -z "$MISE_SETUP_IN_PROGRESS" ] && [ ! -f "opentelemetry-javaagent.jar" ]; then
    export MISE_SETUP_IN_PROGRESS=1
    mise run download-otel --silent
fi
