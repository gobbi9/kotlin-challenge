package it.schwarz.coupon.model.rest

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    val error: String,
)
