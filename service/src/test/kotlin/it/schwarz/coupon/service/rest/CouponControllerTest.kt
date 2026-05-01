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
import io.mockk.clearStaticMockk
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto
import it.schwarz.coupon.service.configuration.serviceJson
import it.schwarz.coupon.service.service.CouponService
import java.math.BigDecimal
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class CouponControllerTest : StringSpec({

    afterTest {
        clearStaticMockk(Span::class)
    }

    "GET /coupons should return OK and coupon list with X-Trace-Id" {
        val couponService = mockk<CouponService>()
        val couponList = CouponListDto(coupons = emptyList(), totalCount = 0, page = 0, pageSize = 100)
        coEvery { couponService.getCoupons(codes = any(), page = any(), pageSize = any()) } returns couponList

        val mockTraceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        mockkStatic(Span::class)
        val mockSpan = mockk<Span>()
        val mockSpanContext = mockk<SpanContext>()
        every { Span.current() } returns mockSpan
        every { mockSpan.spanContext } returns mockSpanContext
        every { mockSpanContext.traceId } returns mockTraceId

        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(serviceJson)
                }
            }
            application {
                // We need to import configureTraceIdHeader or just manual install if it's easier
                // but better use the actual app config if possible.
                // Since configureService() installs it, we can call it.
                // However, configureService() also configures Koin which might conflict with mocks.
                // Let's just install the plugin manually for unit test of the controller.

                install(ServerContentNegotiation) {
                    json(serviceJson)
                }
                install(it.schwarz.coupon.service.configuration.TraceIdHeaderPlugin)
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.get("/coupons")
            response.status shouldBe HttpStatusCode.OK
            response.headers["X-Trace-Id"] shouldBe mockTraceId
            val body = response.body<CouponListDto>()
            body shouldBe couponList
            coVerify(exactly = 1) { couponService.getCoupons(codes = any(), page = 0, pageSize = 100) }
        }
    }

    "POST /coupons should return OK and saved coupon with X-Trace-Id" {
        val couponService = mockk<CouponService>()
        val couponDto = CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test")
        coEvery { couponService.saveCoupon(couponDto = any()) } returns couponDto

        val mockTraceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        mockkStatic(Span::class)
        val mockSpan = mockk<Span>()
        val mockSpanContext = mockk<SpanContext>()
        every { Span.current() } returns mockSpan
        every { mockSpan.spanContext } returns mockSpanContext
        every { mockSpanContext.traceId } returns mockTraceId

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
                install(it.schwarz.coupon.service.configuration.TraceIdHeaderPlugin)
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.post("/coupons") {
                contentType(ContentType.Application.Json)
                setBody(couponDto)
            }
            response.status shouldBe HttpStatusCode.OK
            response.headers["X-Trace-Id"] shouldBe mockTraceId
            val body = response.body<CouponDto>()
            body shouldBe couponDto
            coVerify(exactly = 1) { couponService.saveCoupon(couponDto = any()) }
        }
    }

    "POST /coupons/bulk should return NoContent with X-Trace-Id" {
        val couponService = mockk<CouponService>()
        val coupons = listOf(CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test"))
        coEvery { couponService.saveCoupons(couponDtos = any()) } returns Unit

        val mockTraceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        mockkStatic(Span::class)
        val mockSpan = mockk<Span>()
        val mockSpanContext = mockk<SpanContext>()
        every { Span.current() } returns mockSpan
        every { mockSpan.spanContext } returns mockSpanContext
        every { mockSpanContext.traceId } returns mockTraceId

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
                install(it.schwarz.coupon.service.configuration.TraceIdHeaderPlugin)
                routing {
                    couponRoutes(couponService)
                }
            }

            val response = client.post("/coupons/bulk") {
                contentType(ContentType.Application.Json)
                setBody(coupons)
            }
            response.status shouldBe HttpStatusCode.NoContent
            response.headers["X-Trace-Id"] shouldBe mockTraceId
            coVerify(exactly = 1) { couponService.saveCoupons(couponDtos = any()) }
        }
    }
})
