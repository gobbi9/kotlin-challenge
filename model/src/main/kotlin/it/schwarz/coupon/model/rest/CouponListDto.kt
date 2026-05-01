package it.schwarz.coupon.model.rest

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object representing a paginated list of coupons.
 *
 * This class includes the requested subset of coupons along with pagination
 * information like total count and current page details.
 */
@Serializable
data class CouponListDto(
    /**
     * The list of [CouponDto]s in the current page.
     */
    val coupons: List<CouponDto>,
    /**
     * The total number of coupons available.
     */
    val totalCount: Long,
    /**
     * The current page number.
     */
    val page: Int,
    /**
     * The size of the page.
     */
    val pageSize: Int,
)
