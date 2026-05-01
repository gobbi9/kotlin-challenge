package it.schwarz.coupon.cleanup.configuration

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import it.schwarz.coupon.cleanup.cleaner.CollectionCleaner
import it.schwarz.coupon.cleanup.job.CleanupRunnerJob
import it.schwarz.coupon.cleanup.repository.CleanupCouponRepository
import it.schwarz.coupon.cleanup.service.CleanupRunner
import it.schwarz.coupon.cleanup.service.CouponCleanupRunner
import it.schwarz.coupon.configuration.Database
import it.schwarz.coupon.migrations.MongoMigrations
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.ktor.plugin.KoinApplicationStopPreparing
import org.koin.ktor.plugin.KoinApplicationStopped
import org.koin.logger.slf4jLogger
import java.time.Instant

private val log = KotlinLogging.logger {}

/**
 * Executes MongoDB migrations if the necessary configuration is present.
 */
fun Application.configureMigrations() {
    MongoMigrations.runMigrations()
}

/**
 * Configures Koin dependency injection for the cleanup application.
 *
 * This function sets up the Koin container, defines the necessary modules,
 * and handles the lifecycle of the application by subscribing to Koin events.
 */
fun Application.configureKoin() {
    log.debug { "Configuring dependency injection using Koin" }

    install(Koin) {
        slf4jLogger()
        allowOverride(true)

        val appModule = module(createdAtStart = true) {
            single { environment }.bind(ApplicationEnvironment::class)

            val uri = System.getProperty("MONGODB_URI") ?: System.getenv("MONGODB_URI")
            val name = System.getProperty("DATABASE_NAME") ?: System.getenv("DATABASE_NAME")

            single<MongoClient> {
                requireNotNull(uri) { "database URI not configured" }
                Database().configureDatabase(uri)
            }

            single<MongoDatabase> {
                requireNotNull(name) { "database name not configured" }
                get<MongoClient>().getDatabase(name)
            }

            single {
                CleanupCouponRepository(
                    get<MongoDatabase>(),
                )
            }

            single {
                CollectionCleaner(
                    cleanupCouponRepository = get<CleanupCouponRepository>(),
                )
            }

            val retentionMinutes = System.getProperty("COUPON_RETENTION_MINUTES")?.toLong()
                ?: System.getenv("COUPON_RETENTION_MINUTES")?.toLong()
                ?: 60L

            val currentTime = Instant.now()
            single<CleanupRunner> {
                CouponCleanupRunner(
                    collectionCleaner = get<CollectionCleaner>(),
                    currentTime = currentTime,
                    retentionMinutes = retentionMinutes,
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
