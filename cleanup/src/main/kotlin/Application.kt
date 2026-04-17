package it.schwarz.einvoice.cleanup

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import it.schwarz.einvoice.cleanup.configuration.configureKoin
import it.schwarz.einvoice.cleanup.service.CleanupRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain
        .main(args)
}

@Suppress("unused")
fun Application.module() {
    val logger = KotlinLogging.logger {}

    configureKoin()

    with(monitor) {
        subscribe(ApplicationStarted) {
            logger.info {
                "Application started"
            }

            val cleanupRunner = get<CleanupRunner>()
            cleanupRunner.start()
            get<CoroutineScope>().launch {
                while (cleanupRunner.isRunning()) {
                    delay(5.toLong().seconds)
                }

                logger.info { "CleanupRunner is finished. Stopping application." }
                exitProcess(0)
            }
        }
        subscribe(ApplicationStopped) {
            logger.info { "Stopping application" }
        }
    }
}
