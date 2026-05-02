rootProject.name = "coupon-kotlin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.toml"))
        }
    }
}

include("cleanup")
include("configuration")
include("db-migrations")
include("service")
include("model")
include("frontend")
include("documentation")
