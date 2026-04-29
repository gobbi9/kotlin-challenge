package it.schwarz.coupon.model.mapper

import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto

fun CouponDto.toEntity(): CouponDocument =
    CouponDocument(
        id = id,
        code = code,
        discount = discount,
        description = description,
        applicationCount = applicationCount,
        version = version,
        creationDateTime = creationDateTime,
        updateDateTime = updateDateTime,
    )

fun CouponDocument.toDto(): CouponDto =
    CouponDto(
        id = id,
        code = code,
        discount = discount,
        description = description,
        applicationCount = applicationCount,
        version = version,
        creationDateTime = creationDateTime,
        updateDateTime = updateDateTime,
    )
