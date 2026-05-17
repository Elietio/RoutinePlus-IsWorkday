package xyz.elietio.routineplus.isworkday.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
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

    private val _holidays = MutableStateFlow<List<HolidayEntity>>(emptyList())
    val holidays: StateFlow<List<HolidayEntity>> = _holidays.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _selectedDay = MutableStateFlow<HolidayEntity?>(null)
    val selectedDay: StateFlow<HolidayEntity?> = _selectedDay.asStateFlow()

    init {
        loadMonthData()
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

    fun navigateMonth(offset: Int) {
        _currentMonth.value = _currentMonth.value.plusMonths(offset.toLong())
        loadMonthData()
    }

    fun selectDay(date: LocalDate) {
        viewModelScope.launch {
            _selectedDay.value = repository.getDayByDate(date.toString())
        }
    }

    fun clearSelection() {
        _selectedDay.value = null
    }

    fun sync() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncHolidayUseCase()
            loadMonthData()
            loadSyncStatus()
            _isSyncing.value = false
        }
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            val ym = _currentMonth.value
            val start = ym.atDay(1).toString()
            val end = ym.atEndOfMonth().toString()
            repository.getDaysBetween(start, end).collect { days ->
                _holidays.value = days
            }
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            _lastSyncTime.value = repository.getLastSyncTime()
        }
    }
}
