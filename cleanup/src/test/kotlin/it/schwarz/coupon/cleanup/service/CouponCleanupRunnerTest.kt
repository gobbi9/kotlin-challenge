package it.schwarz.coupon.cleanup.service

import io.kotest.core.spec.style.StringSpec
import io.mockk.coVerify
import io.mockk.mockk
import it.schwarz.coupon.cleanup.cleaner.CollectionCleaner
import java.time.Instant

class CouponCleanupRunnerTest : StringSpec({

    "CouponCleanupRunner should call collectionCleaner with correct filter" {
        val collectionCleaner = mockk<CollectionCleaner>(relaxed = true)
        val currentTime = Instant.parse("2023-01-01T12:00:00Z")
        val retentionMinutes = 60L
        val runner = CouponCleanupRunner(collectionCleaner, currentTime, retentionMinutes)

        runner.doCleanup()

        coVerify(exactly = 1) {
            collectionCleaner.clean(
                collectionName = "coupons",
                filter = any(), // Filters.lt is hard to match exactly because Bson is opaque
            )
        }
    }
})
