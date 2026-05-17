package xyz.elietio.routineplus.isworkday.domain.usecase

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.domain.model.DayType
import javax.inject.Inject

class SetAlarmUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkDayType: CheckDayTypeUseCase
) {
    data class ExecutionResult(
        val dayType: DayType,
        val shouldSetAlarm: Boolean,
        val alarmSet: Boolean,
        val message: String
    )

    suspend operator fun invoke(config: AlarmConfig): ExecutionResult {
        val dayType = checkDayType(config.targetOffset)
        val shouldSet = when (config.conditionMode) {
            ConditionMode.WORKDAY -> dayType == DayType.WORKDAY
            ConditionMode.OFFDAY -> dayType == DayType.OFFDAY
            ConditionMode.ALWAYS -> true
        }

        if (!shouldSet) {
            return ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = false,
                alarmSet = false,
                message = "条件不满足 ($dayType ≠ ${config.conditionMode})，已跳过闹钟创建"
            )
        }

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, config.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, config.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, config.label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, config.skipUi)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = true,
                alarmSet = true,
                message = "闹钟已创建: ${config.hour}:${config.minute.toString().padStart(2, '0')} - ${config.label}"
            )
        } catch (e: Exception) {
            ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = true,
                alarmSet = false,
                message = "闹钟创建失败: ${e.message}"
            )
        }
    }
}
