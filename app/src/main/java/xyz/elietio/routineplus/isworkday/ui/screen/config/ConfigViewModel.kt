package xyz.elietio.routineplus.isworkday.ui.screen.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.data.repository.AlarmRepository
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.usecase.SetAlarmUseCase
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val setAlarmUseCase: SetAlarmUseCase
) : ViewModel() {

    val alarms: StateFlow<List<AlarmConfig>> = alarmRepository.allAlarmsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun addAlarm(config: AlarmConfig) {
        viewModelScope.launch {
            val currentCount = alarmRepository.getAlarmsCount()
            if (currentCount >= 5) {
                _toastEvent.emit("闹钟数量已达上限 (最多 5 个)")
                return@launch
            }
            alarmRepository.insertAlarm(config)
        }
    }

    fun updateAlarm(config: AlarmConfig) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(config)
        }
    }

    fun deleteAlarm(config: AlarmConfig) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(config)
        }
    }

    fun toggleAlarmEnabled(config: AlarmConfig, isEnabled: Boolean) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(config.copy(isEnabled = isEnabled))
        }
    }

    fun testTriggerAll() {
        viewModelScope.launch {
            val enabledAlarms = alarmRepository.getEnabledAlarms()
            if (enabledAlarms.isEmpty()) {
                _toastEvent.emit("未检测到已启用的闹钟")
                return@launch
            }

            var successCount = 0
            var skipCount = 0
            var failCount = 0

            for (config in enabledAlarms) {
                val result = setAlarmUseCase(config)
                if (result.alarmSet) {
                    successCount++
                } else if (!result.shouldSetAlarm) {
                    skipCount++
                } else {
                    failCount++
                }
            }

            val msg = buildString {
                append("测试完毕：已设置 ")
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
            _toastEvent.emit(msg)
        }
    }
}
