package it.schwarz.coupon.service

import io.ktor.server.application.Application
import it.schwarz.coupon.service.configuration.configureService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureService()
}
