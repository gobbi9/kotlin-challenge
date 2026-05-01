package it.schwarz.coupon.cleanup.job

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.server.application.ServerReady
import io.ktor.server.testing.testApplication
import it.schwarz.coupon.cleanup.module
import it.schwarz.coupon.configuration.MongoDatabaseTestcontainer
import it.schwarz.coupon.model.mongodb.CouponDocument
import kotlinx.coroutines.delay
import org.bson.types.ObjectId
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random.Default.nextDouble
import kotlin.time.Duration.Companion.seconds

class CleanupRunnerJobIntegrationTest : StringSpec({
    val mongoDatabaseTestcontainer = MongoDatabaseTestcontainer(
        databaseName = "coupon-db",
        additionalProperties = mapOf("COUPON_RETENTION_MINUTES" to "0"),
    )
    extension(mongoDatabaseTestcontainer)

    "Should cleanup 10.000 coupons using full application context" {
        // Use the database from the container directly to setup data
        val testDatabase = mongoDatabaseTestcontainer.getDatabase()
        val collection = testDatabase.getCollection<CouponDocument>(collectionName = "coupons")

        // 1. Create 10.000 coupons
        val oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES)
        val coupons = (1..10_000).map { i ->
            CouponDocument(
                id = ObjectId(),
                code = "COUPON_$i",
                discount = nextDouble(from = 1.0, until = 100.0).toBigDecimal()
                    .setScale(2, RoundingMode.HALF_UP),
                description = "Test coupon $i",
                creationDateTime = oneMinuteAgo,
                updateDateTime = oneMinuteAgo,
            )
        }
        collection.insertMany(documents = coupons)

        // Verify they are there
        collection.countDocuments() shouldBe 10_000

        testApplication {
            application {
                module()
            }

            // Trigger at least one request to start the application in testApplication
            client.get("/")
            // Manually trigger ServerReady event because testApplication does not trigger it
            application.monitor.raise(definition = ServerReady, value = application.environment)

            // 2. Wait for it to finish.
            var attempts = 0
            while (collection.countDocuments() > 0 && attempts < 15) {
                delay(duration = 1.seconds)
                attempts++
            }

            // 3. Verify they are gone
            collection.countDocuments() shouldBe 0
        }
    }
})
