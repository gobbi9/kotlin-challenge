plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation(libs.mongodb.bson)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
}
