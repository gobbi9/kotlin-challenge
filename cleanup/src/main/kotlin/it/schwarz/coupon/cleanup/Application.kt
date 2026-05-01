package it.schwarz.coupon.cleanup

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ServerReady
import it.schwarz.coupon.cleanup.configuration.configureKoin
import it.schwarz.coupon.cleanup.configuration.configureMigrations
import it.schwarz.coupon.cleanup.job.CleanupRunnerJob
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.get

/**
 * Main entry point of the application.
 */
fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

private val log = KotlinLogging.logger {}

/**
 * Configures the application module.
 *
 * This function is responsible for setting up the application's components,
 * including dependency injection and lifecycle event subscriptions.
 */
@Suppress("unused")
fun Application.module() {
    configureMigrations()
    configureKoin()

    with(monitor) {
        subscribe(ServerReady) {
            log.info { "Server is ready to start cleanup runner job" }

            val cleanupRunner = get<CleanupRunnerJob>()
            runBlocking {
                cleanupRunner.start()
            }
        }
        subscribe(ApplicationStopped) {
            log.info { "Stopping application" }
        }
    }
}
