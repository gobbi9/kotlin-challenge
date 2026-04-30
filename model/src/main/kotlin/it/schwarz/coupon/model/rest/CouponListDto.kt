package it.schwarz.coupon.model.rest

import kotlinx.serialization.Serializable

@Serializable
data class CouponListDto(
    val coupons: List<CouponDto>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
)
