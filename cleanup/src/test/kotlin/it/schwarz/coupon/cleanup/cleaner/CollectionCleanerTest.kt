package it.schwarz.coupon.cleanup.cleaner

import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import org.bson.Document
import org.bson.types.ObjectId

class CollectionCleanerTest : StringSpec({

    "Collection cleaner should delete documents in batches" {
        val repository = mockk<DocumentRepository>()
        val cleaner = CollectionCleaner(repository)
        val collectionName = "testCollection"
        val filter = Document("key", "value")
        val ids = List(1500) { ObjectId() }

        coEvery { repository.findIdsByCreationDateTimeLessThan(collectionName, filter) } returns ids
        coEvery { repository.deleteByIds(collectionName, any()) } returns 1000L andThen 500L

        cleaner.clean(collectionName, filter)

        coVerify(exactly = 1) { repository.findIdsByCreationDateTimeLessThan(collectionName, filter) }
        coVerify(exactly = 1) { repository.deleteByIds(collectionName, ids.subList(0, 1000)) }
        coVerify(exactly = 1) { repository.deleteByIds(collectionName, ids.subList(1000, 1500)) }
    }

    "Collection cleaner should handle empty documents list" {
        val repository = mockk<DocumentRepository>()
        val cleaner = CollectionCleaner(repository)
        val collectionName = "testCollection"
        val filter = Document("key", "value")

        coEvery { repository.findIdsByCreationDateTimeLessThan(collectionName, filter) } returns emptyList()

        cleaner.clean(collectionName, filter)

        coVerify(exactly = 1) { repository.findIdsByCreationDateTimeLessThan(collectionName, filter) }
        coVerify(exactly = 0) { repository.deleteByIds(any(), any()) }
    }
})
