package service

import com.mongodb.client.model.Filters
import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CollectionCleanupRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.bson.conversions.Bson
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CollectionCleanupRunnerTest :
    StringSpec({

        "test do cleanup" {
            runTest {
                val runners =
                    listOf(
                        TestCouponCleanupRunner(delay = 100.milliseconds, scope = this),
                        TestCouponCleanupRunner(delay = 1.minutes, scope = this),
                        TestCouponCleanupRunner(delay = 2.hours, scope = this),
                        TestCouponCleanupRunner(delay = 5.seconds, scope = this),
                    )
                val cleanup = CleanupRunner(cleanupRunners = runners.toTypedArray(), scope = this)

                cleanup.start()
                // wait to be finished
                while (cleanup.isRunning()) {
                    delay(50.milliseconds)
                }

                runners.forEach { assertFalse(it.isRunning()) }
            }
        }
    }) {
    class TestCouponCleanupRunner(
        val delay: Duration,
        scope: CoroutineScope,
    ) : CollectionCleanupRunner(mockk<DocumentRepository>(), scope) {
        private var job: Job? = null

        override fun getCollectionName(): String = "test-collection"

        override fun getFilter(): Bson = Filters.exists("_id")

        override fun doCleanup(): Job {
            val launchedJob =
                scope.launch {
                    delay(delay)
                }
            job = launchedJob
            return launchedJob
        }

        fun isRunning(): Boolean = job?.isActive == true
    }
}
