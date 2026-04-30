package it.schwarz.coupon.service.configuration

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import it.schwarz.coupon.configuration.Database
import it.schwarz.coupon.model.rest.ErrorDto
import it.schwarz.coupon.model.serialization.couponSerializersModule
import it.schwarz.coupon.service.repository.CouponRepository
import it.schwarz.coupon.service.rest.couponRoutes
import it.schwarz.coupon.service.service.CouponService
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

private val log = KotlinLogging.logger {}

val serviceJson = Json {
    serializersModule = couponSerializersModule
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

fun Application.configureService() {
    configureKoin()
    configureSerialization()
    configureStatusPages()
    install(DefaultHeaders)
    configureLogging()
    configureRouting()
}

fun Application.configureKoin() {
    log.debug { "Configuring Koin" }
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { environment }.bind(ApplicationEnvironment::class)

                val uri = System.getProperty("MONGODB_URI") ?: System.getenv("MONGODB_URI")
                val name = System.getProperty("DATABASE_NAME") ?: System.getenv("DATABASE_NAME")

                single<MongoClient> {
                    requireNotNull(uri) { "MONGODB_URI not configured" }
                    Database().configureDatabase(uri)
                }
                single<MongoDatabase> {
                    requireNotNull(name) { "DATABASE_NAME not configured" }
                    get<MongoClient>().getDatabase(name)
                }
                single { CouponRepository(get()) }
                single { CouponService(get()) }
            },
        )
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(serviceJson)
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<CouponService.TooManyCodesException> { call, cause ->
            log.error(cause) { "Bad Request: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorDto(error = cause.message ?: "Bad Request"),
            )
        }
        exception<Throwable> { call, cause ->
            log.error(cause) { "Unhandled exception: ${cause.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorDto(error = cause.message ?: "Internal Server Error"),
            )
        }
    }
}

fun Application.configureLogging() {
    install(CallLogging)
}

fun Application.configureRouting() {
    val couponService by inject<CouponService>()
    routing {
        couponRoutes(couponService)
    }
}
