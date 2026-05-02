import com.github.gradle.node.npm.task.NpmTask

plugins {
    alias(libs.plugins.node.gradle)
}

node {
    version.set(libs.versions.nodeVersion.get())
    download.set(true)
    npmInstallCommand.set(if (file("package-lock.json").exists()) "ci" else "install")
    workDir.set(layout.buildDirectory.dir(".gradle/nodejs"))
    npmWorkDir.set(layout.buildDirectory.dir(".gradle/npm"))
    nodeProjectDir.set(projectDir)
}

tasks.named("npmInstall") {
    inputs.file("package.json")
    if (file("package-lock.json").exists()) {
        inputs.file("package-lock.json")
    }
    outputs.dir("node_modules")
}

val docusaurusGenApiDocs by tasks.registering(NpmTask::class) {
    group = "documentation"
    description = "Generates OpenAPI MDX docs from the service spec."
    dependsOn(tasks.named("npmInstall"))
    args.set(listOf("run", "gen-api-docs"))
    inputs.file(rootProject.file("service/src/main/resources/openapi/documentation.yaml"))
    inputs.file("docusaurus.config.js")
    outputs.dir("docs/api")
}

val docusaurusBuild by tasks.registering(NpmTask::class) {
    group = "build"
    description = "Builds the static Docusaurus documentation site."
    dependsOn(tasks.named("npmInstall"))
    dependsOn(docusaurusGenApiDocs)
    args.set(listOf("run", "build"))
    inputs.dir("docs")
    inputs.dir("src")
    inputs.dir("static")
    inputs.file("docusaurus.config.js")
    inputs.file("sidebars.ts")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    inputs.file(rootProject.file("service/src/main/resources/openapi/documentation.yaml"))
    outputs.dir(layout.buildDirectory.dir("site"))
    outputs.cacheIf { true }
}

tasks.register("build") {
    group = "build"
    description = "Builds the documentation site."
    dependsOn(docusaurusBuild)
}

tasks.register<NpmTask>("docusaurusStart") {
    group = "documentation"
    description = "Runs the Docusaurus dev server (auto-reload)."
    dependsOn(tasks.named("npmInstall"), docusaurusGenApiDocs)
    args.set(listOf("run", "start"))
}

tasks.register<NpmTask>("docusaurusServe") {
    group = "documentation"
    description = "Serves the previously built site."
    dependsOn(docusaurusBuild)
    args.set(listOf("run", "serve"))
}

tasks.register<NpmTask>("docusaurusClean") {
    group = "documentation"
    description = "Removes generated OpenAPI MDX files."
    dependsOn(tasks.named("npmInstall"))
    args.set(listOf("run", "clean-api-docs"))
}

tasks.register<NpmTask>("deploy") {
    group = "documentation"
    description = "Deploys the Docusaurus documentation site."
    dependsOn(tasks.named("build"))
    args.set(listOf("run", "deploy"))
    environment.set(mapOf("USE_SSH" to "true"))
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Cleans the documentation build output and Docusaurus cache."
    delete(layout.buildDirectory)
    delete(".docusaurus")
}
