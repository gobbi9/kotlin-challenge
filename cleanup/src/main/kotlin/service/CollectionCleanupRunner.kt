package it.schwarz.coupon.cleanup.service

import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import org.bson.types.ObjectId

private val log = KotlinLogging.logger { }

const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"

abstract class CollectionCleanupRunner(
    private val documentRepository: DocumentRepository,
    protected val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    abstract fun getCollectionName(): String

    abstract fun getFilter(): Bson

    open fun doCleanup(): Job = scope.launch {
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
