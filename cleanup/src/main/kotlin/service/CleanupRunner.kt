package it.schwarz.coupon.cleanup.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds

private val log = KotlinLogging.logger { }

class CleanupRunner(
    private vararg val cleanupRunners: CollectionCleanupRunner,
) {
    private var running: Boolean = false

    /**
     * Delay before exitProcess(0) in CleanupRunner.start()
     * to allow graceful termination of background event executors and prevent RejectedExecutionException.
     */
    @WithSpan("cleanup-operation")
    suspend fun start() {
        if (running) {
            log.warn { "CleanupRunner is already running!" }
            return
        }
        running = true
        log.info { "CleanupRunner started" }
        coroutineScope {
            cleanupRunners.forEach { runner ->
                launch { runner.doCleanup() }
            }
        }
        running = false
        log.info { "CleanupRunner is finished. Stopping application after 100ms." }
        delay(duration = 100.milliseconds)
        exitProcess(status = 0)
    }
}
