package it.schwarz.coupon.service.repository

import com.mongodb.client.result.InsertManyResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.schwarz.coupon.model.mongodb.CouponDocument
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import org.bson.conversions.Bson
import java.math.BigDecimal
import java.time.Instant

class CouponRepositoryTest : StringSpec({

    "CouponRepository findAll should return list of coupons" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()
        val findFlow = mockk<FindFlow<CouponDocument>>()
        val coupon = CouponDocument(
            code = "TEST",
            discount = BigDecimal.TEN,
            description = "Test",
            creationDateTime = Instant.now(),
            updateDateTime = Instant.now(),
        )
        val slot = slot<FlowCollector<CouponDocument>>()

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection
        every { collection.find() } returns findFlow
        every { findFlow.skip(any()) } returns findFlow
        every { findFlow.limit(any()) } returns findFlow
        coEvery { findFlow.collect(capture(slot)) } coAnswers {
            listOf(coupon).asFlow().collect(slot.captured)
        }

        val repository = CouponRepository(database)
        val result = repository.findAll(skip = 0, limit = 10)

        result shouldBe listOf(coupon)
    }

    "CouponRepository count should return number of coupons" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection
        coEvery { collection.countDocuments(any<Bson>(), any()) } returns 5L

        val repository = CouponRepository(database)
        val result = repository.count()

        result shouldBe 5L
    }

    "CouponRepository findByCodes should return coupons matching codes" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()
        val findFlow = mockk<FindFlow<CouponDocument>>()
        val coupon = CouponDocument(
            code = "TEST",
            discount = BigDecimal.TEN,
            description = "Test",
            creationDateTime = Instant.now(),
            updateDateTime = Instant.now(),
        )
        val slot = slot<FlowCollector<CouponDocument>>()

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection
        every { collection.find(any<Bson>()) } returns findFlow
        coEvery { findFlow.collect(capture(slot)) } coAnswers {
            listOf(coupon).asFlow().collect(slot.captured)
        }

        val repository = CouponRepository(database)
        val result = repository.findByCodes(codes = listOf("TEST"))

        result shouldBe listOf(coupon)
    }

    "CouponRepository save should call collection.insertOne" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()
        val coupon = CouponDocument(
            code = "SAVE_TEST",
            discount = BigDecimal.TEN,
            description = "Save Test",
            creationDateTime = Instant.now(),
            updateDateTime = Instant.now(),
        )

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection
        coEvery { collection.insertOne(any<CouponDocument>(), any()) } returns mockk<InsertOneResult>()

        val repository = CouponRepository(database)
        repository.save(coupon)

        coVerify(exactly = 1) { collection.insertOne(coupon, any()) }
    }

    "CouponRepository saveAll should call collection.insertMany when list is not empty" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()
        val coupons = listOf(
            CouponDocument(
                code = "SAVE_ALL_1",
                discount = BigDecimal.ONE,
                description = "Save All 1",
                creationDateTime = Instant.now(),
                updateDateTime = Instant.now(),
            ),
        )

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection
        coEvery { collection.insertMany(any<List<CouponDocument>>(), any()) } returns mockk<InsertManyResult>()

        val repository = CouponRepository(database)
        repository.saveAll(coupons)

        coVerify(exactly = 1) { collection.insertMany(coupons, any()) }
    }

    "CouponRepository saveAll should not call collection.insertMany when list is empty" {
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<CouponDocument>>()

        every { database.getCollection<CouponDocument>(any<String>()) } returns collection

        val repository = CouponRepository(database)
        repository.saveAll(emptyList())

        coVerify(exactly = 0) { collection.insertMany(any<List<CouponDocument>>(), any()) }
    }
})
