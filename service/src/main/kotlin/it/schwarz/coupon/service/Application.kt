package it.schwarz.coupon.service

import io.ktor.server.application.Application
import it.schwarz.coupon.service.configuration.configureService

/**
 * Main entry point of the coupon service application.
 */
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * Configures the Ktor application module.
 *
 * This function initializes the service by calling [configureService].
 */
@Suppress("unused")
fun Application.module() {
    configureService()
}
