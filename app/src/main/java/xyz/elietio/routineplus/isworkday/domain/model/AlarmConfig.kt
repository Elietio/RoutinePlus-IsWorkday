package xyz.elietio.routineplus.isworkday.domain.model

data class AlarmConfig(
    val id: Int = 0,
    val targetOffset: Int = 1,
    val conditionMode: ConditionMode = ConditionMode.WORKDAY,
    val hour: Int = 8,
    val minute: Int = 30,
    val label: String = "通勤闹钟",
    val skipUi: Boolean = true,
    val isEnabled: Boolean = true,
    val lastAlarmDate: String = "",
    val lastAlarmTimestamp: Long = 0L
)
