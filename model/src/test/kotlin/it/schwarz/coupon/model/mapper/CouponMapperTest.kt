package it.schwarz.coupon.model.mapper

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.LocalDateTime

class CouponMapperTest : StringSpec({

    "toDto should map entity to DTO" {
        val id = ObjectId()
        val creationDateTime = LocalDateTime.now().minusDays(1)
        val updateDateTime = LocalDateTime.now()
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
        val creationDateTime = LocalDateTime.now().minusDays(1)
        val updateDateTime = LocalDateTime.now()
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
})
