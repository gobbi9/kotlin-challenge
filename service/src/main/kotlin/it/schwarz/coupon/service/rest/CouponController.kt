package it.schwarz.coupon.service.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.service.service.CouponService

private val log = KotlinLogging.logger {}

fun Route.couponRoutes(couponService: CouponService) {
    route("/coupons") {
        get {
            log.debug { "GET /coupons" }
            val codes = call.request.queryParameters.getAll(name = "codes")
            val page = call.parameters["page"]?.toIntOrNull() ?: 0
            val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 100
            val couponList = couponService.getCoupons(codes = codes, page = page, pageSize = pageSize)
            call.respond(status = HttpStatusCode.OK, message = couponList)
        }

        post {
            log.debug { "POST /coupons" }
            val coupon = call.receive<CouponDto>()
            val savedCoupon = couponService.saveCoupon(couponDto = coupon)
            call.respond(status = HttpStatusCode.OK, message = savedCoupon)
        }

        post("/bulk") {
            log.debug { "POST /coupons/bulk" }
            val coupons = call.receive<List<CouponDto>>()
            couponService.saveCoupons(couponDtos = coupons)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
