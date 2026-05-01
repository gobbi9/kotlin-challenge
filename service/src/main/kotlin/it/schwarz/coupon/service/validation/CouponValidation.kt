package it.schwarz.coupon.service.validation

import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult
import it.schwarz.coupon.model.rest.CouponDto
import java.math.BigDecimal

fun RequestValidationConfig.validateCouponDto() {
    validate<CouponDto> { coupon ->
        val reasons = mutableListOf<String>()
        if (coupon.code.isBlank()) reasons.add("Code must not be empty or blank")
        if (coupon.discount <= BigDecimal.ZERO) reasons.add("Discount must be a positive number bigger than 0")
        if (coupon.description.isEmpty()) reasons.add("Description must not be empty")

        if (reasons.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(reasons)
        }
    }
}
