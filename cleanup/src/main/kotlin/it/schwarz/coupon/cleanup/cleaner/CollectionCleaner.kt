package it.schwarz.coupon.cleanup.cleaner

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.instrumentation.annotations.WithSpan
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import org.bson.conversions.Bson
import org.bson.types.ObjectId

private val log = KotlinLogging.logger { }

const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"
private const val DEFAULT_BATCH_SIZE = 1000

class CollectionCleaner(
    private val documentRepository: DocumentRepository,
) {
    @WithSpan("clean")
    suspend fun clean(
        collectionName: String,
        filter: Bson,
    ) {
        val ids = getDocuments(collectionName, filter)
        log.info { "Starting cleanup for ${ids.size} documents in $collectionName. Filter: $filter" }
        val batches = ids.chunked(DEFAULT_BATCH_SIZE)

        log.info { "Cleaning up ${ids.size} documents in $collectionName" }

        batches.forEachIndexed { index, batch ->
            log.info { "Batch #${index + 1} of ${batches.size}" }
            val deletedCount = documentRepository.deleteByIds(
                collectionName = collectionName,
                ids = batch,
            )
            log.info { "Deleted $deletedCount documents" }
        }

        log.info { "Cleanup up finished for $collectionName" }
    }

    private suspend fun getDocuments(
        collectionName: String,
        filter: Bson,
    ): List<ObjectId> =
        documentRepository.findIdsByCreationDateTimeLessThan(
            collectionName = collectionName,
            cleanupFilter = filter,
        )
}
