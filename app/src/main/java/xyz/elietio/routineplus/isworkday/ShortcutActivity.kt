package xyz.elietio.routineplus.isworkday

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import xyz.elietio.routineplus.isworkday.data.repository.ConfigRepository
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
    @Inject lateinit var configRepository: ConfigRepository

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
                withTimeoutOrNull(5000) {
                    // 从 DataStore 读取用户保存的配置
                    val config = configRepository.getAlarmConfig()
                    
                    Log.i(TAG, "Execute config: offset=${config.targetOffset}, mode=${config.conditionMode}, " +
                            "time=${config.hour}:${config.minute}, label=${config.label}")

                    // Step 1: 判定日期类型
                    val chinaZone = ZoneId.of("Asia/Shanghai")
                    val targetDate = LocalDate.now(chinaZone).plusDays(config.targetOffset.toLong())
                    val dayType = checkDayTypeUseCase(config.targetOffset)

                    val offsetLabel = if (config.targetOffset == 0) "今天" else "明天"
                    val dayTypeLabel = when (dayType) {
                        DayType.WORKDAY -> "工作日"
                        DayType.OFFDAY -> "休息日"
                        DayType.NORMAL -> "普通日"
                    }

                    Log.i(TAG, "Day check: $targetDate ($offsetLabel) -> $dayTypeLabel")
                    showToast("$offsetLabel $targetDate: $dayTypeLabel")

                    // Step 2: 执行闹钟逻辑
                    val result = setAlarmUseCase(config)
                    Log.i(TAG, "Alarm result: shouldSet=${result.shouldSetAlarm}, " +
                            "alarmSet=${result.alarmSet}, message=${result.message}")

                    // Step 3: 显示结果 Toast (纯文字无符号)
                    val timeStr = "${config.hour}:${config.minute.toString().padStart(2, '0')}"
                    if (result.alarmSet) {
                        showToast("闹钟已设置: $timeStr - ${config.label}")
                    } else if (!result.shouldSetAlarm) {
                        showToast("已跳过: $dayTypeLabel 不符合触发条件")
                    } else {
                        showToast("闹钟创建失败: ${result.message}")
                    }
                } ?: run {
                    Log.w(TAG, "Execute timeout after 5000ms")
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
