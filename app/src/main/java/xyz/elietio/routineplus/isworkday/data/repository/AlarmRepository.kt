package xyz.elietio.routineplus.isworkday.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import xyz.elietio.routineplus.isworkday.data.local.AlarmDao
import xyz.elietio.routineplus.isworkday.data.local.entity.AlarmEntity
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val configRepository: ConfigRepository
) {
    companion object {
        private const val TAG = "AlarmRepository"
    }

    val allAlarmsFlow: Flow<List<AlarmConfig>> = alarmDao.getAllAlarmsFlow()
        .map { entities ->
            entities.map { it.toDomain() }
        }
        .onStart {
            checkAndMigrateOldData()
        }

    suspend fun getEnabledAlarms(): List<AlarmConfig> {
        checkAndMigrateOldData()
        return alarmDao.getEnabledAlarms().map { it.toDomain() }
    }

    suspend fun insertAlarm(config: AlarmConfig): Long {
        return alarmDao.insertAlarm(AlarmEntity.fromDomain(config))
    }

    suspend fun updateAlarm(config: AlarmConfig) {
        alarmDao.updateAlarm(AlarmEntity.fromDomain(config))
    }

    suspend fun deleteAlarm(config: AlarmConfig) {
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(config))
    }

    suspend fun getAlarmsCount(): Int {
        return alarmDao.getAlarmsCount()
    }

    private suspend fun checkAndMigrateOldData() {
        try {
            val count = alarmDao.getAlarmsCount()
            if (count == 0) {
                Log.i(TAG, "No alarms in Room, checking for legacy DataStore config...")
                val legacyConfig = configRepository.getAlarmConfig()
                Log.i(TAG, "Legacy config found: $legacyConfig. Migrating to Room database...")
                alarmDao.insertAlarm(
                    AlarmEntity(
                        targetOffset = legacyConfig.targetOffset,
                        conditionMode = legacyConfig.conditionMode.name,
                        hour = legacyConfig.hour,
                        minute = legacyConfig.minute,
                        label = legacyConfig.label,
                        isEnabled = legacyConfig.isEnabled
                    )
                )
                Log.i(TAG, "Legacy config successfully migrated to Room!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking or migrating legacy data", e)
        }
    }
}
