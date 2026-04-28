package it.schwarz.coupon.cleanup.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val log = KotlinLogging.logger { }

class CleanupRunner(
    private vararg val cleanupRunners: CollectionCleanupRunner,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private var job: Job? = null

    fun start() {
        if (isRunning()) {
            log.warn { "CleanupRunner is already running!" }
            return
        }
        job = scope.launch {
            val jobs = cleanupRunners.map { it.doCleanup() }
            jobs.joinAll()
        }
        log.info { "CleanupRunner started" }
    }

    fun isRunning(): Boolean = job?.isActive == true
}
