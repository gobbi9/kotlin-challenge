package it.schwarz.coupon.service.configuration

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.opentelemetry.api.trace.Span
import it.schwarz.coupon.configuration.Database
import it.schwarz.coupon.migrations.MongoMigrations
import it.schwarz.coupon.model.mapper.toErrorDto
import it.schwarz.coupon.model.serialization.couponSerializersModule
import it.schwarz.coupon.service.repository.CouponRepository
import it.schwarz.coupon.service.rest.couponRoutes
import it.schwarz.coupon.service.service.CouponService
import it.schwarz.coupon.service.validation.validateCouponDto
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

private val log = KotlinLogging.logger {}

/**
 * The [Json] configuration used for serializing and deserializing data in the service.
 * It uses the custom [couponSerializersModule] and is configured to ignore unknown keys.
 */
val serviceJson = Json {
    serializersModule = couponSerializersModule
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

/**
 * Executes MongoDB migrations if the necessary configuration is present.
 */
fun Application.configureMigrations() {
    MongoMigrations.runMigrations()
}

/**
 * Entry point for configuring all aspects of the application service.
 *
 * This function calls individual configuration functions to set up Koin,
 * serialization, validation, status pages, headers, logging, and routing.
 */
fun Application.configureService() {
    configureKoin()
    configureSerialization()
    configureRequestValidation()
    configureStatusPages()
    install(plugin = DefaultHeaders)
    configureTraceIdHeader()
    configureLogging()
    configureRouting()
}

/**
 * Configures Koin dependency injection for the application.
 *
 * Sets up the required modules for database connectivity, repositories, and services.
 */
fun Application.configureKoin() {
    log.debug { "Configuring Koin" }
    install(plugin = Koin) {
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

/**
 * Configures content negotiation using Kotlin serialization.
 */
fun Application.configureSerialization() {
    install(plugin = ContentNegotiation) {
        json(serviceJson)
    }
}

/**
 * Configures request validation for incoming DTOs.
 */
fun Application.configureRequestValidation() {
    install(plugin = RequestValidation) {
        validateCouponDto()
    }
}

/**
 * Configures status pages to handle exceptions and return appropriate HTTP responses.
 */
fun Application.configureStatusPages() {
    install(plugin = StatusPages) {
        exception<RequestValidationException> { call, cause ->
            log.error(throwable = cause) { "Validation Error: ${cause.reasons.joinToString()}" }
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = cause.toErrorDto(fallbackMessage = "Validation failed: ${cause.reasons.joinToString()}"),
            )
        }
        exception<CouponService.TooManyCodesException> { call, cause ->
            log.error(throwable = cause) { "Bad Request: ${cause.message}" }
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = cause.toErrorDto(fallbackMessage = "Bad Request"),
            )
        }
        exception<Throwable> { call, cause ->
            log.error(throwable = cause) { "Unhandled exception: ${cause.message}" }
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = cause.toErrorDto(fallbackMessage = "Internal Server Error"),
            )
        }
    }
}

/**
 * Configures call logging for the application.
 */
fun Application.configureLogging() {
    install(plugin = CallLogging)
}

/**
 * Configures the routing for the application, including coupon routes and Swagger UI.
 */
fun Application.configureRouting() {
    val couponService by inject<CouponService>()
    routing {
        couponRoutes(couponService)
        swaggerUI(path = "swagger")
    }
}

/**
 * A Ktor plugin that adds an "X-Trace-Id" header to the response if a valid OpenTelemetry trace ID is present.
 */
val TraceIdHeaderPlugin = createApplicationPlugin(name = "TraceIdHeader") {
    onCall { call ->
        val traceId = Span.current().spanContext.traceId
        if (traceId != "00000000000000000000000000000000") {
            call.response.headers.append(name = "X-Trace-Id", value = traceId)
        }
    }
}

/**
 * Installs the [TraceIdHeaderPlugin] into the application.
 */
fun Application.configureTraceIdHeader() {
    install(TraceIdHeaderPlugin)
}
