package xyz.elietio.routineplus.isworkday.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.domain.usecase.SetAlarmUseCase
import xyz.elietio.routineplus.isworkday.domain.usecase.SyncHolidayUseCase
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutReceiver : BroadcastReceiver() {

    @Inject lateinit var setAlarmUseCase: SetAlarmUseCase
    @Inject lateinit var syncHolidayUseCase: SyncHolidayUseCase

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received action: ${intent.action}")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_EXECUTE -> handleExecute(intent)
                    ACTION_SYNC -> handleSync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling shortcut", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleExecute(intent: Intent) {
        val config = AlarmConfig(
            targetOffset = intent.getIntExtra("target_offset", 1),
            conditionMode = try {
                ConditionMode.valueOf(intent.getStringExtra("condition_mode") ?: "WORKDAY")
            } catch (e: IllegalArgumentException) {
                ConditionMode.WORKDAY
            },
            hour = intent.getIntExtra("alarm_hour", 8),
            minute = intent.getIntExtra("alarm_minute", 30),
            label = intent.getStringExtra("alarm_label") ?: "通勤闹钟",
            skipUi = intent.getBooleanExtra("skip_ui", true)
        )

        val result = setAlarmUseCase(config)
        Log.i(TAG, "Execute result: ${result.message}")
    }

    private suspend fun handleSync() {
        val result = syncHolidayUseCase()
        Log.i(TAG, "Sync result: ${if (result.isSuccess) "success" else "failed"}")
    }

    companion object {
        private const val TAG = "ShortcutReceiver"
        const val ACTION_EXECUTE = "xyz.elietio.routineplus.isworkday.ACTION_EXECUTE"
        const val ACTION_SYNC = "xyz.elietio.routineplus.isworkday.ACTION_SYNC"
    }
}
