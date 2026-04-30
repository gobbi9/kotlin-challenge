package it.schwarz.coupon.service.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.model.mapper.toCouponListDto
import it.schwarz.coupon.model.mapper.toDto
import it.schwarz.coupon.model.mapper.toEntity
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.model.rest.CouponListDto
import it.schwarz.coupon.service.repository.CouponRepository

private val log = KotlinLogging.logger {}

private const val MAX_PAGE_SIZE = 100

class CouponService(
    private val couponRepository: CouponRepository,
) {
    class TooManyCodesException(message: String) : RuntimeException(message)

    @WithSpan("getCoupons")
    suspend fun getCoupons(
        codes: List<String>?,
        page: Int = 0,
        pageSize: Int = MAX_PAGE_SIZE,
    ): CouponListDto {
        validateSize(coupons = codes)
        log.debug { "Getting coupons. Codes filter: $codes, page=$page, pageSize=$pageSize" }
        return if (codes.isNullOrEmpty()) {
            getAllCoupons(page = page, pageSize = pageSize, skip = page * pageSize)
        } else {
            getCouponsByCodes(codes = codes, page = page, pageSize = pageSize)
        }
    }

    private fun validateSize(coupons: List<*>?) {
        coupons?.let {
            if (it.size > MAX_PAGE_SIZE) {
                throw TooManyCodesException("Too many coupons provided. Maximum allowed is $MAX_PAGE_SIZE")
            }
        }
    }

    private suspend fun getAllCoupons(
        page: Int,
        pageSize: Int,
        skip: Int,
    ): CouponListDto {
        val coupons = couponRepository.findAll(skip, limit = pageSize).map { it.toDto() }
        return coupons.toCouponListDto(
            page = page,
            pageSize = pageSize,
            totalCount = couponRepository.count(),
        )
    }

    private suspend fun getCouponsByCodes(
        codes: List<String>,
        page: Int,
        pageSize: Int,
    ): CouponListDto {
        val coupons = couponRepository.findByCodes(codes).map { it.toDto() }
        return coupons.toCouponListDto(
            page = page,
            pageSize = pageSize,
        )
    }

    @WithSpan("saveCoupon")
    suspend fun saveCoupon(couponDto: CouponDto): CouponDto {
        log.debug { "Saving coupon: ${couponDto.code}" }
        couponRepository.save(couponDto.toEntity())
        return couponDto
    }

    @WithSpan("saveCoupons")
    suspend fun saveCoupons(couponDtos: List<CouponDto>) {
        validateSize(coupons = couponDtos)
        log.debug { "Saving ${couponDtos.size} coupons" }
        couponRepository.saveAll(couponDtos.map { it.toEntity() })
    }
}
