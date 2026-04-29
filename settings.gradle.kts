rootProject.name = "coupon-kotlin"

include("cleanup")
include("api")
project(":api").projectDir = file("service")