package service

import com.mongodb.client.model.Filters
import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CollectionCleanupRunner
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.bson.conversions.Bson
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CollectionCleanupRunnerTest : StringSpec({

    "Cleanup service should start and stop all cleanup runners" {
        runTest {
            val runners = listOf(
                TestCouponCleanupRunner(delay = 100.milliseconds),
                TestCouponCleanupRunner(delay = 1.minutes),
                TestCouponCleanupRunner(delay = 2.hours),
                TestCouponCleanupRunner(delay = 5.seconds),
            )
            val cleanup = CleanupRunner(cleanupRunners = runners.toTypedArray())

            cleanup.start()

            runners.forEach { assertFalse(it.isRunning()) }
        }
    }
}) {
    class TestCouponCleanupRunner(
        val delay: Duration,
    ) : CollectionCleanupRunner(mockk<DocumentRepository>()) {
        private var running: Boolean = false

        override fun getCollectionName(): String = "test-collection"

        override fun getFilter(): Bson = Filters.exists("_id")

        override suspend fun doCleanup() {
            running = true
            delay(delay)
            running = false
        }

        fun isRunning(): Boolean = running
    }
}
