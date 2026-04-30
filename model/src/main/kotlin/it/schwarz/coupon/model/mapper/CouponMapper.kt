package it.schwarz.coupon.model.mapper

import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto

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

fun List<CouponDto>.toCouponListDto(
    page: Int,
    pageSize: Int,
    totalCount: Long = size.toLong(),
): CouponListDto =
    CouponListDto(
        coupons = this,
        totalCount = totalCount,
        page = page,
        pageSize = pageSize,
    )
