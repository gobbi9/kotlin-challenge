package it.schwarz.coupon.cleanup.job

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.service.CleanupRunner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds

private val log = KotlinLogging.logger { }

class CleanupRunnerJob(
    private val cleanupRunners: List<CleanupRunner>,
) {
    private var running: Boolean = false

    /**
     * Delay before exitProcess(0) in CleanupRunnerJob.start()
     * to allow graceful termination of background event executors and prevent RejectedExecutionException.
     */
    @WithSpan("cleanup-operation")
    suspend fun start() {
        if (running) {
            log.warn { "Cleanup runner job is already running!" }
            return
        }
        running = true
        log.info { "Cleanup runner job started" }
        coroutineScope {
            cleanupRunners.forEach { runner ->
                launch { runner.doCleanup() }
            }
        }
        running = false
        log.info { "Cleanup runner job is finished. Stopping application after 100ms." }
        delay(duration = 100.milliseconds)
        exitProcess(status = 0)
    }
}
