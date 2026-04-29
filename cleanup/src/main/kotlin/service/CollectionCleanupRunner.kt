package it.schwarz.coupon.cleanup.service

import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import org.bson.conversions.Bson
import org.bson.types.ObjectId

private val log = KotlinLogging.logger { }

const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"

abstract class CollectionCleanupRunner(
    private val documentRepository: DocumentRepository,
) {
    abstract fun getCollectionName(): String

    abstract fun getFilter(): Bson

    open suspend fun doCleanup() {
        val ids = getDocuments()
        log.info { "Starting cleanup for ${ids.size} documents" }
        val batches = ids.chunked(1000)

        log.info { "Cleaning up ${ids.size} documents in ${getCollectionName()}" }

        batches.forEachIndexed { index, batch ->
            val batchRun = index + 1
            log.info { "Batch #$batchRun of ${batches.size}" }
            val deletedCount = documentRepository.deleteByIds(
                collectionName = getCollectionName(),
                ids = batch,
            )
            log.info { "Deleted $deletedCount documents" }
        }

        log.info { "Cleanup up finished for ${getCollectionName()}" }
    }

    private suspend fun getDocuments(): List<ObjectId> =
        documentRepository.findIdsByCreationDateTimeLessThan(
            getCollectionName(),
            getFilter(),
        )
}
