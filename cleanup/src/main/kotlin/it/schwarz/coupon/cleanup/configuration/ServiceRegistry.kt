package it.schwarz.coupon.cleanup.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import it.schwarz.coupon.cleanup.cleaner.CollectionCleaner
import it.schwarz.coupon.cleanup.job.CleanupRunnerJob
import it.schwarz.coupon.cleanup.repository.DefaultDocumentRepository
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CouponCleanupRunner
import it.schwarz.coupon.configuration.Database
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger
import java.time.Instant

private val log = KotlinLogging.logger {}

fun Application.configureKoin() {
    log.debug { "Configuring dependency injection using Koin" }

    install(Koin) {
        slf4jLogger()

        val appModule =
            module(createdAtStart = true) {
                single { environment }.bind(ApplicationEnvironment::class)
                single { environment.config }.bind(ApplicationConfig::class)

                val uri = requireNotNull(System.getenv("MONGODB_URI")) { "database URI not configured" }
                val name = requireNotNull(System.getenv("DATABASE_NAME")) { "database name not configured" }

                val mongoDatabase = Database().configureDatabase(uri, name)

                single<DocumentRepository> {
                    DefaultDocumentRepository(
                        mongoDatabase,
                    )
                }

                single {
                    CollectionCleaner(
                        documentRepository = get<DocumentRepository>(),
                    )
                }

                val currentTime = Instant.now()
                single<CleanupRunner> {
                    CouponCleanupRunner(
                        collectionCleaner = get<CollectionCleaner>(),
                        currentTime = currentTime,
                        retentionMinutes = System.getenv("COUPON_RETENTION_MINUTES").toLong(),
                    )
                }

                single {
                    CleanupRunnerJob(
                        cleanupRunners = getAll<CleanupRunner>(),
                        application = this@configureKoin,
                    )
                }
            }
        modules(appModule)
    }

    @Suppress("UNUSED")
    with(monitor) {
        subscribe(KoinApplicationStarted) { log.info { "Koin started" } }
        subscribe(KoinApplicationStopPreparing) { log.info { "Koin stopping" } }
        subscribe(KoinApplicationStopped) { log.info { "Koin stopped" } }
    }
}
