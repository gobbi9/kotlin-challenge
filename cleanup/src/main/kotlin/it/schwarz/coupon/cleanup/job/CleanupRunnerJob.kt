package it.schwarz.coupon.cleanup.job

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.service.CleanupRunner

private val log = KotlinLogging.logger { }

class CleanupRunnerJob(
    private val cleanupRunners: List<CleanupRunner>,
    private val application: Application,
) {
    /**
     * There is no point checking if this job is already running, because
     *
     * 1. it will only check if this method is being called again
     * 2. if another container runs this job, it will concurrently anyway
     * 3. delete operations should be idempotent and work no matter how many times they are called concurrently or sequentially
     *
     * Proper coordination or locking must be done separately by an external database or service.
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
