package it.schwarz.coupon.service.rest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import it.schwarz.coupon.configuration.MongoDatabaseTestcontainer
import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto
import it.schwarz.coupon.service.configuration.serviceJson
import it.schwarz.coupon.service.module
import java.math.BigDecimal
import java.time.Instant

class CouponControllerIntegrationTest : StringSpec({
    val mongoDatabaseTestcontainer = MongoDatabaseTestcontainer(
        databaseName = "coupon-db",
    )
    extension(mongoDatabaseTestcontainer)

    "GET /coupons should return coupons from database" {
        val testDatabase = mongoDatabaseTestcontainer.getDatabase()
        val collection = testDatabase.getCollection<CouponDocument>(collectionName = "coupons")
        val coupon = CouponDocument(
            code = "INTEGRATION_TEST",
            discount = BigDecimal.TEN,
            description = "Integration Test",
            creationDateTime = Instant.now(),
            updateDateTime = Instant.now(),
        )
        collection.insertOne(document = coupon)

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.get("/coupons?codes=INTEGRATION_TEST")
            response.status shouldBe HttpStatusCode.OK
            val body = response.body<CouponListDto>()
            body.coupons.size shouldBe 1
            body.coupons.first().code shouldBe "INTEGRATION_TEST"
        }
    }

    "GET /coupons should return all coupons if codes parameter is not provided" {
        val testDatabase = mongoDatabaseTestcontainer.getDatabase()
        val collection = testDatabase.getCollection<CouponDocument>(collectionName = "coupons")
        // The previous test already inserted "INTEGRATION_TEST", so let's add another one
        val coupon = CouponDocument(
            code = "ALL_COUPONS_TEST",
            discount = BigDecimal.ONE,
            description = "All Coupons Test",
            creationDateTime = Instant.now(),
            updateDateTime = Instant.now(),
        )
        collection.insertOne(document = coupon)

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.get("/coupons")
            response.status shouldBe HttpStatusCode.OK
            val body = response.body<CouponListDto>()
            // Expecting at least the 2 coupons we know about (from this test and the previous one if it ran)
            // However, kotest StringSpec might run tests in isolation or sequentially sharing the DB.
            // MongoDatabaseTestcontainer doesn't seem to drop the DB between tests in the same spec.
            body.coupons.any { it.code == "ALL_COUPONS_TEST" } shouldBe true
            body.coupons.any { it.code == "INTEGRATION_TEST" } shouldBe true
        }
    }

    "POST /coupons should save coupon to database and return it" {
        val couponDto = CouponDto(code = "POST_TEST", discount = BigDecimal.valueOf(15.5), description = "Post Test")

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.post("/coupons") {
                contentType(ContentType.Application.Json)
                setBody(couponDto)
            }
            response.status shouldBe HttpStatusCode.OK
            val body = response.body<CouponDto>()
            body.code shouldBe "POST_TEST"
            body.discount shouldBe BigDecimal.valueOf(15.5)
        }

        val collection = mongoDatabaseTestcontainer.getDatabase().getCollection<CouponDocument>(collectionName = "coupons")
        collection.countDocuments() shouldBe 3
    }

    "POST /coupons/bulk should save multiple coupons" {
        val coupons = listOf(
            CouponDto(code = "BULK_1", discount = BigDecimal.ONE, description = "Bulk 1"),
            CouponDto(code = "BULK_2", discount = BigDecimal.TEN, description = "Bulk 2"),
        )

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.post("/coupons/bulk") {
                contentType(ContentType.Application.Json)
                setBody(coupons)
            }
            response.status shouldBe HttpStatusCode.NoContent
        }

        val collection = mongoDatabaseTestcontainer.getDatabase().getCollection<CouponDocument>(collectionName = "coupons")
        collection.countDocuments() shouldBe 5
    }

    "GET /coupons should return 400 Bad Request if more than 100 codes are provided" {
        val codes = List(101) { "CODE_$it" }

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.get("/coupons?${codes.joinToString("&") { "codes=$it" }}")
            response.status shouldBe HttpStatusCode.BadRequest
        }
    }

    "POST /coupons/bulk should return 400 Bad Request if more than 100 coupons are provided" {
        val coupons = List(101) {
            CouponDto(code = "CODE_$it", discount = BigDecimal.TEN, description = "Desc $it")
        }

        testApplication {
            application {
                module()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(serviceJson)
                }
            }

            val response = client.post("/coupons/bulk") {
                contentType(ContentType.Application.Json)
                setBody(coupons)
            }
            response.status shouldBe HttpStatusCode.BadRequest
        }
    }
})
