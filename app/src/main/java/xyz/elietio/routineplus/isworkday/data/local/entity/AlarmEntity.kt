package xyz.elietio.routineplus.isworkday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetOffset: Int,
    val conditionMode: String,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean = true,
    val lastAlarmDate: String = "",
    val lastAlarmTimestamp: Long = 0L
) {
    fun toDomain(): AlarmConfig = AlarmConfig(
        id = id,
        targetOffset = targetOffset,
        conditionMode = try { ConditionMode.valueOf(conditionMode) } catch (e: Exception) { ConditionMode.WORKDAY },
        hour = hour,
        minute = minute,
        label = label,
        isEnabled = isEnabled,
        lastAlarmDate = lastAlarmDate,
        lastAlarmTimestamp = lastAlarmTimestamp
    )

    companion object {
        fun fromDomain(config: AlarmConfig): AlarmEntity = AlarmEntity(
            id = config.id,
            targetOffset = config.targetOffset,
            conditionMode = config.conditionMode.name,
            hour = config.hour,
            minute = config.minute,
            label = config.label,
            isEnabled = config.isEnabled,
            lastAlarmDate = config.lastAlarmDate,
            lastAlarmTimestamp = config.lastAlarmTimestamp
        )
    }
}
