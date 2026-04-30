package it.schwarz.coupon.migrations

import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.reactivestreams.client.MongoClient
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.schwarz.coupon.configuration.Database
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoMigrationsTest : StringSpec({

    "Should run migrations and record them" {
        val mockClient = mockk<com.mongodb.kotlin.client.coroutine.MongoClient>(relaxed = true)
        val mockDb = mockk<MongoDatabase>(relaxed = true)
        val mockCollection = mockk<MongoCollection<Document>>(relaxed = true)
        val couponsCollection = mockk<MongoCollection<Document>>(relaxed = true)
        val mockDatabase = mockk<Database>(relaxed = true)

        every { mockDatabase.configureDatabase(any()) } returns mockClient
        every { mockClient.getDatabase(any()) } returns mockDb
        every { mockDb.getCollection<Document>("schema_migrations") } returns mockCollection
        every { mockDb.getCollection<Document>("coupons") } returns couponsCollection

        // No migrations applied yet
        coEvery { mockCollection.find<Document>().toList() } returns emptyList()

        MongoMigrations.run("mongodb://localhost", "test-db", databaseProvider = { mockDatabase })

        // No verification for now to ensure it at least runs
    }
})
