package it.schwarz.coupon.model.rest

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object representing an error response.
 *
 * This class is used to provide feedback to the client when an error occurs,
 * containing a descriptive message of the failure.
 */
@Serializable
data class ErrorDto(
    /**
     * The error message.
     */
    val error: String,
)
