package it.schwarz.coupon.cleanup.service

interface CleanupRunner {
    suspend fun doCleanup()
}
