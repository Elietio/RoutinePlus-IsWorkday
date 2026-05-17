package xyz.elietio.routineplus.isworkday.ui.screen.sandbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.domain.model.DayType
import xyz.elietio.routineplus.isworkday.domain.usecase.CheckDayTypeUseCase
import xyz.elietio.routineplus.isworkday.domain.usecase.SetAlarmUseCase
import xyz.elietio.routineplus.isworkday.data.repository.HolidayRepository
import xyz.elietio.routineplus.isworkday.ui.component.TerminalLevel
import xyz.elietio.routineplus.isworkday.ui.component.TerminalLine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SandboxViewModel @Inject constructor(
    private val checkDayType: CheckDayTypeUseCase,
    private val setAlarmUseCase: SetAlarmUseCase,
    private val repository: HolidayRepository
) : ViewModel() {

    private val chinaZone = ZoneId.of("Asia/Shanghai")

    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun runSimulation(
        simulatedDate: LocalDate?,
        targetOffset: Int,
        conditionMode: ConditionMode,
        hour: Int,
        minute: Int,
        label: String
    ) {
        viewModelScope.launch {
            _isRunning.value = true
            _lines.value = emptyList()

            addLine("系统", "启动 R+ 模拟沙盒...", TerminalLevel.INFO)
            addLine("注入", "锁定标准时区: Asia/Shanghai (UTC+8)", TerminalLevel.INFO)

            val baseDate = simulatedDate ?: LocalDate.now(chinaZone)
            addLine("模拟", "基准日期: $baseDate", TerminalLevel.INFO)

            val targetDate = baseDate.plusDays(targetOffset.toLong())
            addLine("读取", "参数 -> 目标偏移量: +${targetOffset} 天, 条件: $conditionMode", TerminalLevel.INFO)
            addLine("计算", "目标校验日: $targetDate (${getDayOfWeekChinese(targetDate)})", TerminalLevel.INFO)

            val record = repository.getDayByDate(targetDate.toString())
            val dayType = checkDayType.checkDate(targetDate)

            if (record != null) {
                addLine("本地", "查询数据库命中 -> \"${record.name}\", isOffDay: ${record.isOffDay}", TerminalLevel.SUCCESS)
            } else {
                val isWeekend = targetDate.dayOfWeek.value >= 6
                addLine("本地", "数据库无特殊记录, 按星期判定 -> ${if (isWeekend) "周末" else "工作日"}", TerminalLevel.INFO)
            }

            addLine("判定", "日期类型: $dayType", TerminalLevel.INFO)

            val shouldSet = when (conditionMode) {
                ConditionMode.WORKDAY -> dayType == DayType.WORKDAY
                ConditionMode.OFFDAY -> dayType == DayType.OFFDAY
                ConditionMode.ALWAYS -> true
            }

            if (shouldSet) {
                addLine("结果", "条件满足：将创建闹钟 $hour:${minute.toString().padStart(2, '0')} - $label", TerminalLevel.SUCCESS)
            } else {
                addLine("判定", "不满足 $conditionMode 触发条件", TerminalLevel.WARNING)
                addLine("结果", "拦截成功：已跳过闹钟创建。", TerminalLevel.WARNING)
            }

            _isRunning.value = false
        }
    }

    fun runRealTest(config: AlarmConfig) {
        viewModelScope.launch {
            _isRunning.value = true
            _lines.value = emptyList()

            addLine("系统", "真实闹钟测试模式", TerminalLevel.WARNING)
            addLine("执行", "正在调用系统闹钟 API...", TerminalLevel.INFO)

            val result = setAlarmUseCase(config)

            if (result.alarmSet) {
                addLine("结果", result.message, TerminalLevel.SUCCESS)
            } else {
                val level = if (result.shouldSetAlarm) TerminalLevel.ERROR else TerminalLevel.WARNING
                addLine("结果", result.message, level)
            }

            _isRunning.value = false
        }
    }

    fun clearTerminal() {
        _lines.value = emptyList()
    }

    private fun addLine(tag: String, message: String, level: TerminalLevel) {
        _lines.value = _lines.value + TerminalLine(tag, message, level)
    }

    private fun getDayOfWeekChinese(date: LocalDate): String {
        return when (date.dayOfWeek.value) {
            1 -> "星期一"; 2 -> "星期二"; 3 -> "星期三"; 4 -> "星期四"
            5 -> "星期五"; 6 -> "星期六"; 7 -> "星期日"; else -> ""
        }
    }
}
