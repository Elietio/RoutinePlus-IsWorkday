package xyz.elietio.routineplus.isworkday.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.OverrideEntity
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import xyz.elietio.routineplus.isworkday.domain.usecase.SyncHolidayUseCase
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HolidayRepository,
    private val syncHolidayUseCase: SyncHolidayUseCase
) : ViewModel() {

    private val chinaZone = ZoneId.of("Asia/Shanghai")

    private val _currentMonth = MutableStateFlow(YearMonth.now(chinaZone))
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val holidays: StateFlow<List<HolidayEntity>> = _currentMonth
        .flatMapLatest { ym ->
            val start = ym.minusMonths(2).atDay(1).toString()
            val end = ym.plusMonths(2).atEndOfMonth().toString()
            repository.getDaysBetween(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _overrides: StateFlow<List<OverrideEntity>> = _currentMonth
        .flatMapLatest { ym ->
            val start = ym.minusMonths(2).atDay(1).toString()
            val end = ym.plusMonths(2).atEndOfMonth().toString()
            repository.getOverridesBetween(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val overridesMap: StateFlow<Map<String, OverrideEntity>> = _overrides
        .map { list -> list.associateBy { it.date } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _selectedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val selectedDates: StateFlow<Set<LocalDate>> = _selectedDates.asStateFlow()

    init {
        loadSyncStatus()
        checkAndInitialSync()
    }

    private fun checkAndInitialSync() {
        viewModelScope.launch {
            if (!repository.hasData()) {
                sync()
            }
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        if (_currentMonth.value != yearMonth) {
            _currentMonth.value = yearMonth
            clearSelectedDates()
        }
    }

    fun navigateMonth(offset: Int) {
        selectMonth(_currentMonth.value.plusMonths(offset.toLong()))
    }

    fun toggleDateSelection(date: LocalDate) {
        val current = _selectedDates.value
        if (current.contains(date)) {
            _selectedDates.value = current - date
        } else {
            _selectedDates.value = current + date
        }
    }

    fun clearSelectedDates() {
        _selectedDates.value = emptySet()
    }

    fun sync() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncHolidayUseCase()
            loadSyncStatus()
            _isSyncing.value = false
        }
    }

    fun applyOverrides(overrideType: Int, customHour: Int?, customMinute: Int?) {
        viewModelScope.launch {
            val dates = _selectedDates.value
            if (dates.isEmpty()) return@launch

            if (overrideType == 0) {
                repository.deleteOverrides(dates.map { it.toString() })
            } else {
                val list = dates.map { date ->
                    OverrideEntity(
                        date = date.toString(),
                        overrideType = overrideType,
                        customHour = if (overrideType == 1) customHour else null,
                        customMinute = if (overrideType == 1) customMinute else null
                    )
                }
                repository.insertOverrides(list)
            }
            clearSelectedDates()
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            _lastSyncTime.value = repository.getLastSyncTime()
        }
    }
}
