# Cleanup-Service (Kotlin)

## Project description

This project is a Kotlin migration of the Java Cleanup-Service (coupon-java) that was started but not finished yet. It was planned to split up in two modules for separation of concerns (API to manage coupons and the cleanup scheduler).

## Setup local development environment

### Precondition:

- Local Podman (https://podman.io) or Docker available
- Bruno installed (https://www.usebruno.com)
- MongoDB Compass (https://www.mongodb.com/products/tools/compass)

1. Provide the MongoDB through docker-compose with your preferred tool (Podman/Docker)
2. Run Cleanup (run configuration for IntelliJ provided) and/or implemented API application
3. Import coupon-api as Bruno collection