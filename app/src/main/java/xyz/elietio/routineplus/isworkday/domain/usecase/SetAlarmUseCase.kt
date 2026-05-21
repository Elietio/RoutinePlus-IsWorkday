package xyz.elietio.routineplus.isworkday.domain.usecase

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.elietio.routineplus.isworkday.data.repository.ConfigRepository
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.domain.model.DayType
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class SetAlarmUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkDayType: CheckDayTypeUseCase,
    private val repository: HolidayRepository,
    private val configRepository: ConfigRepository
) {
    private val chinaZone = ZoneId.of("Asia/Shanghai")

    data class ExecutionResult(
        val dayType: DayType,
        val shouldSetAlarm: Boolean,
        val alarmSet: Boolean,
        val message: String
    )

    suspend operator fun invoke(config: AlarmConfig): ExecutionResult {
        val targetDate = LocalDate.now(chinaZone).plusDays(config.targetOffset.toLong())
        val dayType = checkDayType(config.targetOffset)
        
        val override = repository.getOverrideByDate(targetDate.toString())
        
        val shouldSet: Boolean
        val alarmHour: Int
        val alarmMinute: Int
        val overrideReason: String?

        if (override != null && override.overrideType != 0) {
            if (override.overrideType == 1) {
                shouldSet = true
                alarmHour = override.customHour ?: config.hour
                alarmMinute = override.customMinute ?: config.minute
                overrideReason = if (override.customHour != null && override.customMinute != null) {
                    "用户覆盖(强制设定闹钟, 自定义时间: ${alarmHour}:${alarmMinute.toString().padStart(2, '0')})"
                } else {
                    "用户覆盖(强制设定闹钟, 默认时间)"
                }
            } else {
                shouldSet = false
                alarmHour = config.hour
                alarmMinute = config.minute
                overrideReason = "用户覆盖(强制忽略闹钟)"
            }
        } else {
            shouldSet = when (config.conditionMode) {
                ConditionMode.WORKDAY -> dayType == DayType.WORKDAY
                ConditionMode.OFFDAY -> dayType == DayType.OFFDAY
                ConditionMode.ALWAYS -> true
            }
            alarmHour = config.hour
            alarmMinute = config.minute
            overrideReason = null
        }

        if (!shouldSet) {
            val reasonMsg = overrideReason ?: "条件不满足 ($dayType ≠ ${config.conditionMode})"
            return ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = false,
                alarmSet = false,
                message = "$reasonMsg，已跳过闹钟创建"
            )
        }

        // ── Idempotence & Anti-Flicker Protection ──
        val lastAlarm = configRepository.getLastAlarmInfo()
        val timeDiff = System.currentTimeMillis() - lastAlarm.timestamp
        val isSameDate = lastAlarm.date == targetDate.toString()
        val isSameTime = lastAlarm.hour == alarmHour && lastAlarm.minute == alarmMinute
        val isSameLabel = lastAlarm.label == config.label

        if ((isSameDate && isSameTime && isSameLabel) || timeDiff < 5000L) {
            val duplicateMsg = if (timeDiff < 5000L) {
                "触发过频，防闪烁拦截: ${alarmHour}:${alarmMinute.toString().padStart(2, '0')}"
            } else {
                "闹钟已就绪: ${alarmHour}:${alarmMinute.toString().padStart(2, '0')}"
            }
            android.util.Log.i("SetAlarmUseCase", "Idempotence / Anti-Flicker protection triggered: $duplicateMsg")
            return ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = true,
                alarmSet = true,
                message = duplicateMsg
            )
        }

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, alarmHour)
                putExtra(AlarmClock.EXTRA_MINUTES, alarmMinute)
                putExtra(AlarmClock.EXTRA_MESSAGE, config.label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, config.skipUi)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Cache successful alarm setup parameters
            configRepository.saveLastAlarmInfo(
                ConfigRepository.LastAlarmInfo(
                    date = targetDate.toString(),
                    hour = alarmHour,
                    minute = alarmMinute,
                    label = config.label,
                    timestamp = System.currentTimeMillis()
                )
            )

            val successMsg = if (overrideReason != null) {
                "闹钟已创建(覆盖): ${alarmHour}:${alarmMinute.toString().padStart(2, '0')} - ${config.label}"
            } else {
                "闹钟已创建: ${alarmHour}:${alarmMinute.toString().padStart(2, '0')} - ${config.label}"
            }

            ExecutionResult(
                dayType = dayType,
                shouldSetAlarm = true,
                alarmSet = true,
                message = successMsg
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
