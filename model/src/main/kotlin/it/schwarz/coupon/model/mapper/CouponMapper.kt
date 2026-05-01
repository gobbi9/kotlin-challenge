package it.schwarz.coupon.model.mapper

import it.schwarz.coupon.model.mongodb.CouponDocument
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto
import it.schwarz.coupon.model.rest.ErrorDto

/**
 * Converts a [Throwable] to an [ErrorDto].
 */
fun Throwable.toErrorDto(fallbackMessage: String): ErrorDto =
    ErrorDto(error = this.message ?: fallbackMessage)

/**
 * Converts a [CouponDto] to a [CouponDocument].
 */
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

/**
 * Converts a [CouponDocument] to a [CouponDto].
 */
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

/**
 * Converts a list of [CouponDto]s to a [CouponListDto].
 */
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
