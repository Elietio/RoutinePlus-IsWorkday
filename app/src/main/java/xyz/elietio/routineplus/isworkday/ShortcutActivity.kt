package xyz.elietio.routineplus.isworkday

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import xyz.elietio.routineplus.isworkday.data.repository.AlarmRepository
import xyz.elietio.routineplus.isworkday.domain.model.DayType
import xyz.elietio.routineplus.isworkday.domain.usecase.CheckDayTypeUseCase
import xyz.elietio.routineplus.isworkday.domain.usecase.SetAlarmUseCase
import xyz.elietio.routineplus.isworkday.domain.usecase.SyncHolidayUseCase
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * 透明 Activity，用于处理 Shortcut 调用。
 * Android Shortcuts 只能指向 Activity，不能指向 BroadcastReceiver。
 * 三星日常程序通过 startActivity() 触发快捷方式。
 */
@AndroidEntryPoint
class ShortcutActivity : ComponentActivity() {

    @Inject lateinit var setAlarmUseCase: SetAlarmUseCase
    @Inject lateinit var checkDayTypeUseCase: CheckDayTypeUseCase
    @Inject lateinit var syncHolidayUseCase: SyncHolidayUseCase
    @Inject lateinit var alarmRepository: AlarmRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "ShortcutActivity launched with action: ${intent.action}")

        when (intent.action) {
            ACTION_EXECUTE -> handleExecute()
            ACTION_SYNC -> handleSync()
            else -> {
                Log.w(TAG, "Unknown action: ${intent.action}")
                showToast("未知操作: ${intent.action}")
                finish()
            }
        }
    }

    private fun handleExecute() {
        lifecycleScope.launch {
            try {
                withTimeoutOrNull(10000) {
                    val enabledAlarms = alarmRepository.getEnabledAlarms()
                    if (enabledAlarms.isEmpty()) {
                        Log.i(TAG, "No enabled alarms found")
                        showToast("未检测到已启用的闹钟")
                        return@withTimeoutOrNull
                    }

                    Log.i(TAG, "Execute started for ${enabledAlarms.size} enabled alarms")

                    var successCount = 0
                    var skipCount = 0
                    var failCount = 0

                    val enabledAlarmsSize = enabledAlarms.size
                    for (i in 0 until enabledAlarmsSize) {
                        val config = enabledAlarms[i]
                        Log.i(
                            TAG, "Processing alarm: id=${config.id}, mode=${config.conditionMode}, " +
                                    "time=${config.hour}:${config.minute}, label=${config.label}"
                        )
                        val result = setAlarmUseCase(config)
                        Log.i(
                            TAG, "Alarm result: label=${config.label}, shouldSet=${result.shouldSetAlarm}, " +
                                    "alarmSet=${result.alarmSet}, message=${result.message}"
                        )

                        if (result.alarmSet) {
                            successCount++
                            // 如果还有后续的闹钟需要继续判定处理，在向系统时钟成功发送前一个 Intent 后，
                            // 必须主动延迟一秒（1000ms），为系统时钟处理异步 Activity 启动留出充足窗口，
                            // 从而防止前一个请求被后面紧随的 Intent 发生物理覆盖或被系统吞掉。
                            if (i < enabledAlarmsSize - 1) {
                                Log.i(TAG, "Delaying 1000ms before processing the next alarm to avoid system overlapping")
                                kotlinx.coroutines.delay(1000L)
                            }
                        } else if (!result.shouldSetAlarm) {
                            skipCount++
                        } else {
                            failCount++
                        }
                    }

                    val resultMsg = buildString {
                        append("闹钟判定完毕：已设置 ")
                        append(successCount)
                        append(" 个，跳过 ")
                        append(skipCount)
                        append(" 个")
                        if (failCount > 0) {
                            append("，失败 ")
                            append(failCount)
                            append(" 个")
                        }
                    }
                    showToast(resultMsg)
                } ?: run {
                    Log.w(TAG, "Execute timeout after 10000ms")
                    showToast("执行超时已结束")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Execute failed", e)
                showToast("执行异常: ${e.message}")
            } finally {
                finish()
            }
        }
    }

    private fun handleSync() {
        Log.i(TAG, "Starting manual sync via shortcut")
        showToast("正在同步节假日数据")

        lifecycleScope.launch {
            try {
                withTimeoutOrNull(10000) {
                    val result = syncHolidayUseCase()
                    if (result.isSuccess) {
                        Log.i(TAG, "Sync completed successfully")
                        showToast("节假日数据同步成功")
                    } else {
                        Log.w(TAG, "Sync failed", result.exceptionOrNull())
                        showToast("同步失败: ${result.exceptionOrNull()?.message}")
                    }
                } ?: run {
                    Log.w(TAG, "Sync timeout after 10000ms")
                    showToast("数据同步超时，已在后台运行")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync error", e)
                showToast("同步异常: ${e.message}")
            } finally {
                finish()
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "ShortcutActivity"
        const val ACTION_EXECUTE = "xyz.elietio.routineplus.isworkday.ACTION_EXECUTE"
        const val ACTION_SYNC = "xyz.elietio.routineplus.isworkday.ACTION_SYNC"
    }
}
