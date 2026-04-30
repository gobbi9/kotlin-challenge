package it.schwarz.coupon.cleanup.repository

import com.mongodb.client.result.DeleteResult
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class DocumentRepositoryTest : StringSpec({

    "DocumentRepository findIdsByCreationDateTimeLessThan should return list of ObjectIds" {
        val mongoDatabase = mockk<MongoDatabase>()
        val repository = DocumentRepository(mongoDatabase)
        val collectionName = "testCollection"
        val filter = mockk<Bson>()
        val mockCollection = mockk<MongoCollection<Document>>()
        val mockFindFlow = mockk<FindFlow<Document>>()
        val id1 = ObjectId()
        val id2 = ObjectId()
        val doc1 = Document("_id", id1)
        val doc2 = Document("_id", id2)
        val slot = slot<FlowCollector<Document>>()

        every { mongoDatabase.getCollection<Document>(collectionName) } returns mockCollection
        every { mockCollection.find(filter) } returns mockFindFlow
        every { mockFindFlow.projection(any()) } returns mockFindFlow

        coEvery { mockFindFlow.collect(capture(slot)) } coAnswers {
            listOf(doc1, doc2).asFlow().collect(slot.captured)
        }

        val result = repository.findIdsByCreationDateTimeLessThan(collectionName, filter)

        result shouldBe listOf(id1, id2)
    }

    "DocumentRepository deleteByIds should return deleted count" {
        val mongoDatabase = mockk<MongoDatabase>()
        val repository = DocumentRepository(mongoDatabase)
        val collectionName = "testCollection"
        val ids = listOf(ObjectId(), ObjectId())
        val mockCollection = mockk<MongoCollection<Document>>()
        val deleteResult = mockk<DeleteResult>()

        every { mongoDatabase.getCollection<Document>(collectionName) } returns mockCollection
        coEvery { mockCollection.deleteMany(any<Bson>(), any()) } returns deleteResult
        every { deleteResult.deletedCount } returns 2L

        val result = repository.deleteByIds(collectionName, ids)

        result shouldBe 2L
    }
})
