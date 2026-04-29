package it.schwarz.coupon.cleanup.job

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.service.CleanupRunner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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
        coroutineScope {
            cleanupRunners.forEach { runner ->
                launch { runner.doCleanup() }
            }
        }
        log.info { "Cleanup runner job is finished. Stopping application in 1 second." }
        coroutineScope {
            launch {
                delay(duration = 1.seconds) // avoid RejectedExecutionException in logs
                application.engine.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
            }
        }
    }
}
