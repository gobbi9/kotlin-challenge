package it.schwarz.coupon.cleanup.job

import io.kotest.core.spec.style.StringSpec
import io.mockk.coVerify
import io.mockk.mockk
import it.schwarz.coupon.cleanup.service.CleanupRunner
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CleanupRunnerJobTest : StringSpec({

    "Cleanup runner job start and stop all cleanup runners" {
        runTest {
            val runners = listOf(
                TestCleanupRunner(delay = 100.milliseconds),
                TestCleanupRunner(delay = 1.minutes),
                TestCleanupRunner(delay = 2.hours),
                TestCleanupRunner(delay = 5.seconds),
            )
            val cleanup = CleanupRunnerJob(cleanupRunners = runners)

            cleanup.start()

            runners.forEach { assertFalse(it.isRunning()) }
        }
    }

    "Cleanup runner job should start all runners" {
        val runner1 = mockk<CleanupRunner>(relaxed = true)
        val runner2 = mockk<CleanupRunner>(relaxed = true)
        val cleanupJob = CleanupRunnerJob(listOf(runner1, runner2))

        cleanupJob.start()

        coVerify(exactly = 1) { runner1.doCleanup() }
        coVerify(exactly = 1) { runner2.doCleanup() }
    }
})

class TestCleanupRunner(
    val delay: Duration,
) : CleanupRunner {
    private var running: Boolean = false

    override suspend fun doCleanup() {
        running = true
        delay(delay)
        running = false
    }

    fun isRunning(): Boolean = running
}
