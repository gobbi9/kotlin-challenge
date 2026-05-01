package it.schwarz.coupon.model.mapper

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class CouponMapperTest : StringSpec({

    "toDto should map entity to DTO" {
        val id = ObjectId()
        val creationDateTime = Instant.now().minus(1, ChronoUnit.DAYS)
        val updateDateTime = Instant.now()
        val entity = CouponDocument(
            id = id,
            code = "PROMO20",
            discount = BigDecimal("20.00"),
            description = "20% discount",
            applicationCount = 5,
            version = 2,
            creationDateTime = creationDateTime,
            updateDateTime = updateDateTime,
        )

        val dto = entity.toDto()

        dto.id shouldBe id
        dto.code shouldBe "PROMO20"
        dto.discount shouldBe BigDecimal("20.00")
        dto.description shouldBe "20% discount"
        dto.applicationCount shouldBe 5
        dto.version shouldBe 2
        dto.creationDateTime shouldBe creationDateTime
        dto.updateDateTime shouldBe updateDateTime
    }

    "toEntity should map DTO to entity" {
        val id = ObjectId()
        val creationDateTime = Instant.now().minus(1, ChronoUnit.DAYS)
        val updateDateTime = Instant.now()
        val dto = CouponDto(
            id = id,
            code = "SALE10",
            discount = BigDecimal("10.50"),
            description = "10.50 discount",
            applicationCount = 0,
            version = 1,
            creationDateTime = creationDateTime,
            updateDateTime = updateDateTime,
        )

        val entity = dto.toEntity()

        entity.id shouldBe id
        entity.code shouldBe "SALE10"
        entity.discount shouldBe BigDecimal("10.50")
        entity.description shouldBe "10.50 discount"
        entity.applicationCount shouldBe 0
        entity.version shouldBe 1
        entity.creationDateTime shouldBe creationDateTime
        entity.updateDateTime shouldBe updateDateTime
    }

    "toCouponListDto should map list of DTOs to CouponListDto" {
        val dtos = listOf(
            CouponDto(code = "C1", discount = BigDecimal.ONE, description = "D1"),
            CouponDto(code = "C2", discount = BigDecimal.TEN, description = "D2"),
        )
        val page = 1
        val pageSize = 10
        val totalCount = 100L

        val result = dtos.toCouponListDto(page = page, pageSize = pageSize, totalCount = totalCount)

        result.coupons shouldBe dtos
        result.page shouldBe page
        result.pageSize shouldBe pageSize
        result.totalCount shouldBe totalCount
    }

    "toCouponListDto should use list size as default totalCount" {
        val dtos = listOf(
            CouponDto(code = "C1", discount = BigDecimal.ONE, description = "D1"),
        )

        val result = dtos.toCouponListDto(page = 0, pageSize = 100)

        result.totalCount shouldBe 1L
    }

    "Throwable.toErrorDto should use exception message when present" {
        val exception = RuntimeException("Custom error message")
        val result = exception.toErrorDto("Fallback message")

        result.error shouldBe "Custom error message"
    }

    "Throwable.toErrorDto should use fallback message when exception message is null" {
        val exception = RuntimeException()
        val result = exception.toErrorDto("Fallback message")

        result.error shouldBe "Fallback message"
    }
})
