package xyz.elietio.routineplus.isworkday.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import xyz.elietio.routineplus.isworkday.data.local.HolidayDao
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.SyncMetaEntity
import xyz.elietio.routineplus.isworkday.data.remote.HolidayApiService
import java.security.MessageDigest
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val dao: HolidayDao,
    private val apiService: HolidayApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val TAG = "HolidayRepository"
        val KEY_PRIMARY_URL = stringPreferencesKey("primary_url")
        val KEY_FALLBACK_URL = stringPreferencesKey("fallback_url")
        const val DEFAULT_PRIMARY_URL = "https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master"
        const val DEFAULT_FALLBACK_URL = "https://fastly.jsdelivr.net/gh/NateScarlet/holiday-cn@master"
        val CHINA_ZONE: ZoneId = ZoneId.of("Asia/Shanghai")
    }

    fun getDaysBetween(startDate: String, endDate: String): Flow<List<HolidayEntity>> {
        return dao.getDaysBetween(startDate, endDate)
    }

    suspend fun getDayByDate(date: String): HolidayEntity? {
        return dao.getDayByDate(date)
    }

    suspend fun hasData(): Boolean = dao.getCount() > 0

    suspend fun syncYear(year: Int): Result<Unit> {
        val prefs = dataStore.data.first()
        val primaryUrl = prefs[KEY_PRIMARY_URL] ?: DEFAULT_PRIMARY_URL
        val fallbackUrl = prefs[KEY_FALLBACK_URL] ?: DEFAULT_FALLBACK_URL

        return try {
            val response = try {
                apiService.fetchHolidayData(primaryUrl, year)
            } catch (e: Exception) {
                Log.w(TAG, "Primary URL failed for year $year, trying fallback", e)
                apiService.fetchHolidayData(fallbackUrl, year)
            }

            val hash = computeHash(response.days.toString())
            val existingMeta = dao.getSyncMeta(year)

            if (existingMeta?.dataHash == hash) {
                Log.d(TAG, "Year $year data unchanged, skipping write")
                return Result.success(Unit)
            }

            val entities = response.days.map { day ->
                HolidayEntity(
                    date = day.date,
                    name = day.name,
                    isOffDay = day.isOffDay,
                    year = year
                )
            }

            dao.deleteByYear(year)
            dao.insertAll(entities)
            dao.upsertSyncMeta(
                SyncMetaEntity(
                    year = year,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    dataHash = hash
                )
            )

            Log.i(TAG, "Synced year $year: ${entities.size} days")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync year $year", e)
            Result.failure(e)
        }
    }

    suspend fun syncCurrentAndNextYear(): Result<Unit> {
        val currentYear = java.time.LocalDate.now(CHINA_ZONE).year
        val r1 = syncYear(currentYear)
        val r2 = syncYear(currentYear + 1)
        return if (r1.isSuccess || r2.isSuccess) Result.success(Unit)
        else Result.failure(r1.exceptionOrNull() ?: Exception("Sync failed"))
    }

    suspend fun getLastSyncTime(): Long? {
        val currentYear = java.time.LocalDate.now(CHINA_ZONE).year
        return dao.getSyncMeta(currentYear)?.lastSyncTimestamp
    }

    fun getPrimaryUrl(): Flow<String> = dataStore.data.map { it[KEY_PRIMARY_URL] ?: DEFAULT_PRIMARY_URL }
    fun getFallbackUrl(): Flow<String> = dataStore.data.map { it[KEY_FALLBACK_URL] ?: DEFAULT_FALLBACK_URL }

    private fun computeHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
