package it.schwarz.coupon.cleanup.service

/**
 * Interface defining a cleanup task.
 *
 * Classes implementing this interface are responsible for performing specific
 * cleanup operations, such as deleting expired data from the database.
 */
interface CleanupRunner {
    /**
     * Performs the cleanup operation.
     */
    suspend fun doCleanup()
}
