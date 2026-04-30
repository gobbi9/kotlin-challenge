package it.schwarz.coupon.service.rest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto
import it.schwarz.coupon.service.configuration.serviceJson
import it.schwarz.coupon.service.service.CouponService
import java.math.BigDecimal
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class CouponControllerTest : StringSpec({

    "GET /coupons should return OK and coupon list" {
        val couponService = mockk<CouponService>()
        val couponList = CouponListDto(coupons = emptyList(), totalCount = 0, page = 0, pageSize = 100)
        coEvery { couponService.getCoupons(codes = any(), page = any(), pageSize = any()) } returns couponList

        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(serviceJson)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(serviceJson)
                }
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.get("/coupons") {
                contentType(ContentType.Application.Json)
                setBody(emptyList<String>())
            }
            response.status shouldBe HttpStatusCode.OK
            val body = response.body<CouponListDto>()
            body shouldBe couponList
            coVerify(exactly = 1) { couponService.getCoupons(codes = any(), page = 0, pageSize = 100) }
        }
    }

    "POST /coupons should return OK and saved coupon" {
        val couponService = mockk<CouponService>()
        val couponDto = CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test")
        coEvery { couponService.saveCoupon(couponDto = any()) } returns couponDto

        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(serviceJson)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(serviceJson)
                }
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.post("/coupons") {
                contentType(ContentType.Application.Json)
                setBody(couponDto)
            }
            response.status shouldBe HttpStatusCode.OK
            val body = response.body<CouponDto>()
            body shouldBe couponDto
            coVerify(exactly = 1) { couponService.saveCoupon(couponDto = any()) }
        }
    }

    "POST /coupons/bulk should return NoContent" {
        val couponService = mockk<CouponService>()
        val coupons = listOf(CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test"))
        coEvery { couponService.saveCoupons(couponDtos = any()) } returns Unit

        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(serviceJson)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(serviceJson)
                }
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.post("/coupons/bulk") {
                contentType(ContentType.Application.Json)
                setBody(coupons)
            }
            response.status shouldBe HttpStatusCode.NoContent
            coVerify(exactly = 1) { couponService.saveCoupons(couponDtos = any()) }
        }
    }
})
