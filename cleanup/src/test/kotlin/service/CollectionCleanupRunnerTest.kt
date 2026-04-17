package service

import com.mongodb.client.model.Filters
import io.kotest.core.spec.style.StringSpec
import io.mockk.mockk
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CollectionCleanupRunner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CollectionCleanupRunnerTest: StringSpec({

    "test do cleanup" {
        val runners = listOf(
            TestCouponCleanupRunner(100.milliseconds),
            TestCouponCleanupRunner(1.minutes),
            TestCouponCleanupRunner(2.hours),
            TestCouponCleanupRunner(5.seconds),
        )

        val cleanup = CleanupRunner(runners[0], runners[1], runners[2], runners[3])

        cleanup.start()
        // wait to be finished
        while (cleanup.isRunning()) {
            delay(50.milliseconds)
        }

        runners.forEach { assertFalse(it.isRunning()) }
    }

}) {

    class TestCouponCleanupRunner(
        val delay: Duration,
    ): CollectionCleanupRunner(mockk<DocumentRepository>()) {
        private var job: Job? = null
        override fun getCollectionName(): String = "test-collection"

        override fun getFilter(): Bson = Filters.exists("_id")

        override fun doCleanup() {
            job = scope.launch {
                delay(delay)
            }
        }

        fun isRunning(): Boolean = job?.isActive == true
    }
}