package it.schwarz.coupon.cleanup.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import it.schwarz.coupon.cleanup.repository.DocumentRepositoryImpl
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CouponCleanupRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger
import java.time.Instant

fun Application.configureKoin() {
    val logger = KotlinLogging.logger {}

    logger.debug { "configuring dependency injection" }

    install(Koin) {
        slf4jLogger()

        val appModule =
            module(createdAtStart = true) {
                single { CoroutineScope(Dispatchers.Default) }.bind(CoroutineScope::class)
                single { environment }.bind(ApplicationEnvironment::class)
                single { environment.config }.bind(ApplicationConfig::class)

                val uri = requireNotNull(System.getenv("MONGODB_URI")) { "database URI not configured" }
                val name = requireNotNull(System.getenv("DATABASE_NAME")) { "database name not configured" }

                val mongoDatabase = Database().configureDatabase(uri, name)

                single<DocumentRepository> {
                    DocumentRepositoryImpl(
                        mongoDatabase,
                    )
                }

                val currentTime = Instant.now()
                single {
                    CouponCleanupRunner(
                        documentRepository = get<DocumentRepository>(),
                        currentTime = currentTime,
                        retentionMinutes = System.getenv("COUPON_RETENTION_MINUTES").toLong(),
                    )
                }

                single {
                    CleanupRunner(
                        get<CouponCleanupRunner>(),
                    )
                }
            }
        modules(appModule)
    }

    @Suppress("UNUSED")
    with(monitor) {
        subscribe(KoinApplicationStarted) { logger.info { "Koin started" } }
        subscribe(KoinApplicationStopPreparing) { logger.info { "Koin stopping" } }
        subscribe(KoinApplicationStopped) { logger.info { "Koin stopped" } }
    }
}
