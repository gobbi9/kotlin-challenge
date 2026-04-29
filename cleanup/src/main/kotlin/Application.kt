package it.schwarz.coupon.cleanup

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import it.schwarz.coupon.cleanup.configuration.configureKoin
import it.schwarz.coupon.cleanup.service.CleanupRunner
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

private val log = KotlinLogging.logger {}

@Suppress("unused")
fun Application.module() {
    configureKoin()

    with(monitor) {
        subscribe(ApplicationStarted) {
            log.info { "Application started" }

            val cleanupRunner = get<CleanupRunner>()
            launch {
                cleanupRunner.start()
            }
        }
        subscribe(ApplicationStopped) {
            log.info { "Stopping application" }
        }
    }
}
