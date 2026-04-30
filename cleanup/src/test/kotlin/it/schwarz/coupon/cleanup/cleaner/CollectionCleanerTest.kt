package it.schwarz.coupon.cleanup.cleaner

import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.schwarz.coupon.cleanup.repository.CleanupCouponRepository
import org.bson.Document

class CollectionCleanerTest : StringSpec({

    "Collection cleaner should delete documents by filter" {
        val repository = mockk<CleanupCouponRepository>()
        val cleaner = CollectionCleaner(repository)
        val collectionName = "testCollection"
        val filter = Document("key", "value")

        coEvery { repository.deleteByFilter(collectionName, filter) } returns 1500L

        cleaner.clean(collectionName, filter)

        coVerify(exactly = 1) { repository.deleteByFilter(collectionName, filter) }
    }
})
