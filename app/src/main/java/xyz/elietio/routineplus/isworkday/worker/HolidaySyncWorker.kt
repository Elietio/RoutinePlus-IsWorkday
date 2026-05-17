package xyz.elietio.routineplus.isworkday.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository

@HiltWorker
class HolidaySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HolidayRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting periodic holiday sync")
        return try {
            val result = repository.syncCurrentAndNextYear()
            if (result.isSuccess) {
                Log.i(TAG, "Periodic sync completed successfully")
                Result.success()
            } else {
                Log.w(TAG, "Periodic sync failed, will retry")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Periodic sync error", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HolidaySyncWorker"
    }
}
