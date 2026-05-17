package xyz.elietio.routineplus.isworkday.ui.screen.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.data.repository.ConfigRepository
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    val config: StateFlow<AlarmConfig> = configRepository.alarmConfigFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlarmConfig()
        )

    fun updateTargetOffset(offset: Int) {
        viewModelScope.launch {
            val current = configRepository.getAlarmConfig()
            configRepository.saveAlarmConfig(current.copy(targetOffset = offset))
        }
    }

    fun updateConditionMode(mode: ConditionMode) {
        viewModelScope.launch {
            val current = configRepository.getAlarmConfig()
            configRepository.saveAlarmConfig(current.copy(conditionMode = mode))
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val current = configRepository.getAlarmConfig()
            configRepository.saveAlarmConfig(current.copy(hour = hour, minute = minute))
        }
    }

    fun updateLabel(label: String) {
        viewModelScope.launch {
            val current = configRepository.getAlarmConfig()
            configRepository.saveAlarmConfig(current.copy(label = label))
        }
    }
}
