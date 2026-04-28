package it.schwarz.coupon.cleanup.service

import io.github.oshai.kotlinlogging.KotlinLogging
import it.schwarz.coupon.cleanup.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import org.bson.types.ObjectId

abstract class CollectionCleanupRunner(
    private val documentRepository: DocumentRepository,
    protected val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val logger = KotlinLogging.logger { }

    abstract fun getCollectionName(): String

    abstract fun getFilter(): Bson

    companion object {
        const val CREATION_DATE_TIME_FIELD_NAME = "creationDateTime"
    }

    open fun doCleanup(): Job =
        scope.launch {
            val ids = getDocuments()
            logger.info { "Starting cleanup for ${ids.size} documents" }
            val batches = ids.chunked(1000)

            logger.info { "Cleaning up ${ids.size} documents in ${getCollectionName()}" }

            batches.forEachIndexed { index, batch ->
                val batchRun = index + 1
                logger.info { "Batch #$batchRun of ${batches.size}" }
                val deletedCount = documentRepository.deleteByIds(getCollectionName(), batch)
                logger.info { "Deleted $deletedCount documents" }
            }

            logger.info { "Cleanup up finished for ${getCollectionName()}" }
        }

    private suspend fun getDocuments(): List<ObjectId> =
        documentRepository.findIdsByCreationDateTimeLessThan(
            getCollectionName(),
            getFilter(),
        )
}
