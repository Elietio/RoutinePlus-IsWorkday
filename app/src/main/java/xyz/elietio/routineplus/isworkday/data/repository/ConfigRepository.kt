package xyz.elietio.routineplus.isworkday.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_TARGET_OFFSET = intPreferencesKey("target_offset")
        val KEY_CONDITION_MODE = stringPreferencesKey("condition_mode")
        val KEY_HOUR = intPreferencesKey("hour")
        val KEY_MINUTE = intPreferencesKey("minute")
        val KEY_LABEL = stringPreferencesKey("label")
        val KEY_SKIP_UI = booleanPreferencesKey("skip_ui")
    }

    val alarmConfigFlow: Flow<AlarmConfig> = dataStore.data.map { prefs ->
        AlarmConfig(
            targetOffset = prefs[KEY_TARGET_OFFSET] ?: 1,
            conditionMode = try {
                ConditionMode.valueOf(prefs[KEY_CONDITION_MODE] ?: "WORKDAY")
            } catch (e: Exception) { ConditionMode.WORKDAY },
            hour = prefs[KEY_HOUR] ?: 8,
            minute = prefs[KEY_MINUTE] ?: 30,
            label = prefs[KEY_LABEL] ?: "通勤闹钟",
            skipUi = prefs[KEY_SKIP_UI] ?: true
        )
    }

    suspend fun getAlarmConfig(): AlarmConfig {
        return alarmConfigFlow.first()
    }

    suspend fun saveAlarmConfig(config: AlarmConfig) {
        dataStore.edit { prefs ->
            prefs[KEY_TARGET_OFFSET] = config.targetOffset
            prefs[KEY_CONDITION_MODE] = config.conditionMode.name
            prefs[KEY_HOUR] = config.hour
            prefs[KEY_MINUTE] = config.minute
            prefs[KEY_LABEL] = config.label
            prefs[KEY_SKIP_UI] = config.skipUi
        }
    }
}
