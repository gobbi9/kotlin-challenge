import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sonarqube) apply false
    jacoco
}

allprojects {
    group = "it.schwarz"
    version = "0.0.1"

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }
    }

    afterEvaluate {
        if (plugins.hasPlugin("org.jlleitschuh.gradle.ktlint")) {
            extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
                verbose.set(true)
                outputToConsole.set(true)
                coloredOutput.set(true)
                reporters {
                    reporter(ReporterType.CHECKSTYLE)
                }
                filter {
                    exclude("**/generated/**")
                    exclude("**/style-violations.kt")
                    include("**/kotlin/**")
                }
            }
        }

        if (plugins.hasPlugin("jacoco")) {
            jacoco {
                reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco/"))
            }

            tasks.withType<Test> {
                useJUnitPlatform()
                jvmArgs("-Xshare:off")
                finalizedBy(tasks.withType<JacocoReport>())
            }

            tasks.withType<JacocoReport> {
                reports {
                    xml.required.set(true)
                    csv.required.set(false)
                    html.required.set(true)
                    xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/report.xml"))
                    html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
                }
            }
        }

        tasks.filter { it.name == "sonar" || it.name == "sonarqube" }.forEach { task ->
            task.doFirst {
                val sonarExtension = extensions.findByName("sonarqube")
                if (sonarExtension != null) {
                    val getProperties = sonarExtension.javaClass.getMethod("getProperties")
                    val p = getProperties.invoke(sonarExtension) as org.sonarqube.gradle.SonarProperties
                    p.property("sonar.coverage.exclusions", listOf("**/src/main/kotlin/FileToExclude.kt"))
                    p.property("sonar.cpd.exclusions", "**/src/main/kotlin/core/domain/*.kt")
                    p.property("sonar.projectName", "kotlin-ktor")
                }
            }
        }
    }
}
