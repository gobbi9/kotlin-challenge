package it.schwarz.coupon.cleanup.repository

import com.mongodb.client.result.DeleteResult
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.bson.Document

class DocumentRepositoryTest : StringSpec({

    "CleanupCouponRepository deleteByFilter should return deleted count" {
        val mongoDatabase = mockk<MongoDatabase>()
        val repository = CleanupCouponRepository(mongoDatabase)
        val collectionName = "testCollection"
        val filter = Document("key", "value")
        val mockCollection = mockk<MongoCollection<Document>>()
        val deleteResult = mockk<DeleteResult>()

        every { mongoDatabase.getCollection<Document>(collectionName) } returns mockCollection
        coEvery { mockCollection.deleteMany(filter, any()) } returns deleteResult
        every { deleteResult.deletedCount } returns 5L

        val result = repository.deleteByFilter(collectionName, filter)

        result shouldBe 5L
    }
})
