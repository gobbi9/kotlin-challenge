package it.schwarz.coupon.service.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.schwarz.coupon.model.rest.CouponDto
import it.schwarz.coupon.service.repository.CouponRepository
import java.math.BigDecimal

class CouponServiceTest : StringSpec({
    val couponRepository = mockk<CouponRepository>()
    val couponService = CouponService(couponRepository)

    "getCoupons should throw TooManyCodesException when codes size exceeds MAX_PAGE_SIZE" {
        val codes = List(101) { "code$it" }

        val exception = shouldThrow<CouponService.TooManyCodesException> {
            couponService.getCoupons(codes = codes)
        }

        exception.message shouldBe "Too many coupons provided. Maximum allowed is 100"
    }

    "getCoupons should call findByCodes when codes are provided" {
        val codes = listOf("COUPON1")
        coEvery { couponRepository.findByCodes(codes = codes) } returns emptyList()

        couponService.getCoupons(codes = codes)

        coVerify(exactly = 1) { couponRepository.findByCodes(codes = codes) }
    }

    "getCoupons should call findAll and count when codes are not provided" {
        coEvery { couponRepository.findAll(skip = 0, limit = 100) } returns emptyList()
        coEvery { couponRepository.count() } returns 0L

        couponService.getCoupons(codes = null)

        coVerify(exactly = 1) { couponRepository.findAll(skip = 0, limit = 100) }
        coVerify(exactly = 1) { couponRepository.count() }
    }

    "saveCoupon should call repository save and return DTO" {
        val couponDto = CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test")
        coEvery { couponRepository.save(any()) } returns Unit

        val result = couponService.saveCoupon(couponDto = couponDto)

        result shouldBe couponDto
        coVerify(exactly = 1) { couponRepository.save(any()) }
    }

    "saveCoupons should throw TooManyCodesException when coupons size exceeds MAX_PAGE_SIZE" {
        val coupons = List(101) { mockk<CouponDto> { every { code } returns "code$it" } }

        val exception = shouldThrow<CouponService.TooManyCodesException> {
            couponService.saveCoupons(couponDtos = coupons)
        }

        exception.message shouldBe "Too many coupons provided. Maximum allowed is 100"
    }

    "saveCoupons should call repository saveAll when validation passes" {
        val coupons = listOf(CouponDto(code = "TEST", discount = BigDecimal.TEN, description = "Test"))
        coEvery { couponRepository.saveAll(any()) } returns Unit

        couponService.saveCoupons(couponDtos = coupons)

        coVerify(exactly = 1) { couponRepository.saveAll(any()) }
    }
})
