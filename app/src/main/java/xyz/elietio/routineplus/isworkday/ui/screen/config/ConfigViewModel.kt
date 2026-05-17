package xyz.elietio.routineplus.isworkday.ui.screen.config

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.shortcut.ShortcutHelper
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val shortcutHelper: ShortcutHelper
) : ViewModel() {

    private val _config = MutableStateFlow(AlarmConfig())
    val config: StateFlow<AlarmConfig> = _config.asStateFlow()

    private val _published = MutableStateFlow(false)
    val published: StateFlow<Boolean> = _published.asStateFlow()

    fun updateTargetOffset(offset: Int) {
        _config.update { it.copy(targetOffset = offset) }
    }

    fun updateConditionMode(mode: ConditionMode) {
        _config.update { it.copy(conditionMode = mode) }
    }

    fun updateTime(hour: Int, minute: Int) {
        _config.update { it.copy(hour = hour, minute = minute) }
    }

    fun updateLabel(label: String) {
        _config.update { it.copy(label = label) }
    }

    fun publishShortcut() {
        shortcutHelper.publishAlarmShortcut(_config.value)
        shortcutHelper.publishSyncShortcut()
        _published.value = true
    }

    fun resetPublished() {
        _published.value = false
    }
}
