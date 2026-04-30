package it.schwarz.coupon.service.serialization

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.service.configuration.serviceJson
import kotlinx.serialization.decodeFromString
import java.math.BigDecimal

class CouponDtoSerializationTest : StringSpec({

    "CouponDto should be decodable from the json example in KDoc" {
        val jsonExample =
            """
            { "code": "abc123", "discount": 12.54, "description": "best coupon ev4", "applicationCount": 0 }
            """.trimIndent()

        val decoded = serviceJson.decodeFromString<CouponDto>(jsonExample)

        decoded.code shouldBe "abc123"
        decoded.discount shouldBe BigDecimal("12.54")
        decoded.description shouldBe "best coupon ev4"
        decoded.applicationCount shouldBe 0
    }
})
