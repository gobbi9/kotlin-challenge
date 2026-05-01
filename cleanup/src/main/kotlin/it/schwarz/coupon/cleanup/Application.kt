package it.schwarz.coupon.cleanup

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ServerReady
import it.schwarz.coupon.cleanup.configuration.configureKoin
import it.schwarz.coupon.cleanup.job.CleanupRunnerJob
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

private val log = KotlinLogging.logger {}

@Suppress("unused")
fun Application.module() {
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
