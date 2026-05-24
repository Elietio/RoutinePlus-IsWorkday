package xyz.elietio.routineplus.isworkday.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.elietio.routineplus.isworkday.data.local.entity.AlarmEntity

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmsCount(): Int
}
