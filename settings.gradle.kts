rootProject.name = "coupon-kotlin"

include("cleanup")
include("configuration")
include("db-migrations")
include("api")
project(":api").projectDir = file("service")