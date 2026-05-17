package xyz.elietio.routineplus.isworkday.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.SyncMetaEntity

@Dao
interface HolidayDao {

    @Query("SELECT * FROM holiday_days WHERE date = :date LIMIT 1")
    suspend fun getDayByDate(date: String): HolidayEntity?

    @Query("SELECT * FROM holiday_days WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDaysBetween(startDate: String, endDate: String): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holiday_days WHERE year = :year ORDER BY date ASC")
    suspend fun getDaysByYear(year: Int): List<HolidayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<HolidayEntity>)

    @Query("DELETE FROM holiday_days WHERE year = :year")
    suspend fun deleteByYear(year: Int)

    @Query("SELECT COUNT(*) FROM holiday_days")
    suspend fun getCount(): Int

    // --- Sync Meta ---

    @Query("SELECT * FROM sync_meta WHERE year = :year LIMIT 1")
    suspend fun getSyncMeta(year: Int): SyncMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncMeta(meta: SyncMetaEntity)
}
