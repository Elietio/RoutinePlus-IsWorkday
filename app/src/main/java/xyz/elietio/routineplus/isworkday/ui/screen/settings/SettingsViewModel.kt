package xyz.elietio.routineplus.isworkday.ui.screen.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _primaryUrl = MutableStateFlow(HolidayRepository.DEFAULT_PRIMARY_URL)
    val primaryUrl: StateFlow<String> = _primaryUrl.asStateFlow()

    private val _fallbackUrl = MutableStateFlow(HolidayRepository.DEFAULT_FALLBACK_URL)
    val fallbackUrl: StateFlow<String> = _fallbackUrl.asStateFlow()

    private val _themeMode = MutableStateFlow(0) // 0 = System, 1 = Light, 2 = Dark
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _primaryUrl.value = prefs[HolidayRepository.KEY_PRIMARY_URL] ?: HolidayRepository.DEFAULT_PRIMARY_URL
            _fallbackUrl.value = prefs[HolidayRepository.KEY_FALLBACK_URL] ?: HolidayRepository.DEFAULT_FALLBACK_URL
            _themeMode.value = prefs[HolidayRepository.KEY_THEME_MODE] ?: 0
        }
    }

    fun updatePrimaryUrl(url: String) { _primaryUrl.value = url }
    fun updateFallbackUrl(url: String) { _fallbackUrl.value = url }
    fun updateThemeMode(mode: Int) { _themeMode.value = mode }

    fun save() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[HolidayRepository.KEY_PRIMARY_URL] = _primaryUrl.value
                prefs[HolidayRepository.KEY_FALLBACK_URL] = _fallbackUrl.value
                prefs[HolidayRepository.KEY_THEME_MODE] = _themeMode.value
            }
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}
