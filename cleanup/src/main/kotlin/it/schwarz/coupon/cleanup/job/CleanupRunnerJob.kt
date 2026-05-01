package it.schwarz.coupon.cleanup.job

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.service.CleanupRunner

private val log = KotlinLogging.logger { }

/**
 * Job responsible for executing all registered cleanup runners.
 *
 * This class iterates through a list of [CleanupRunner]s and triggers their cleanup logic.
 * After all runners have completed, it initiates the shutdown of the application.
 */
class CleanupRunnerJob(
    private val cleanupRunners: List<CleanupRunner>,
    private val application: Application,
) {
    /**
     * Starts the cleanup job and executes each runner.
     *
     * Note: This method does not prevent concurrent executions. Idempotency should be handled
     * by the underlying operations or external coordination.
     */
    @WithSpan("cleanup-operation")
    suspend fun start() {
        log.info { "Cleanup runner job started" }
        log.info { "Number of runners to execute: ${cleanupRunners.size}" }
        cleanupRunners.forEach { runner ->
            log.debug { "Launching runner: ${runner::class.simpleName}" }
            runner.doCleanup()
        }
        log.info { "Cleanup runner job is finished. Stopping application in a few seconds." }
        application.engine.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
    }
}
