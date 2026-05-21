package xyz.elietio.routineplus.isworkday.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.OverrideEntity
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRed
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRedLight
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRedDark
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRedTextDark
import xyz.elietio.routineplus.isworkday.ui.theme.workdayGreen
import xyz.elietio.routineplus.isworkday.ui.theme.workdayGreenLight
import xyz.elietio.routineplus.isworkday.ui.theme.workdayGreenDark
import xyz.elietio.routineplus.isworkday.ui.theme.workdayGreenTextDark
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isOverride: Boolean,
    isOffDay: Boolean?,
    label: String?,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    // ── Morandi Premium Color Adapters ──
    val holidayBg = if (isDark) holidayRedDark else holidayRedLight
    val holidayText = if (isDark) holidayRedTextDark else holidayRed
    
    val workdayBg = if (isDark) workdayGreenDark else workdayGreenLight
    val workdayText = if (isDark) workdayGreenTextDark else workdayGreen

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isOffDay == true -> holidayText
        isOffDay == false -> workdayText
        isWeekend -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val bgModifier = when {
        isSelected -> Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
        isToday -> Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        isOffDay == true && isCurrentMonth -> Modifier.background(holidayBg, CircleShape)
        isOffDay == false && isCurrentMonth -> Modifier.background(workdayBg, CircleShape)
        else -> Modifier
    }

    val borderModifier = when {
        isSelected -> Modifier // Filled state, no border
        isOverride && isCurrentMonth ->
            Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), CircleShape)
        else -> Modifier
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .then(bgModifier)
            .clickable(enabled = isCurrentMonth) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick(date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            if (label != null && isCurrentMonth) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else if (isOffDay == true) holidayText else workdayText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 8.sp
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: java.time.YearMonth,
    holidays: List<HolidayEntity>,
    overridesMap: Map<String, OverrideEntity>,
    selectedDates: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"))
    val firstDay = yearMonth.atDay(1)
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    val weekDayLabels = listOf("日", "一", "二", "三", "四", "五", "六")
    val holidayMap = holidays.associateBy { it.date }

    Column(modifier = modifier.fillMaxWidth()) {
        // Weekday header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDayLabels.forEachIndexed { index, label ->
                val isWeekendLabel = index == 0 || index == 6
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isWeekendLabel) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day grid
        var dayCounter = 1
        val totalWeeks = (firstDayOfWeek + daysInMonth + 6) / 7

        for (week in 0 until totalWeeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dow in 0..6) {
                    val cellIndex = week * 7 + dow
                    if (cellIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                        val displayDate = if (cellIndex < firstDayOfWeek) {
                            firstDay.minusDays((firstDayOfWeek - cellIndex).toLong())
                        } else {
                            yearMonth.atEndOfMonth().plusDays(
                                (cellIndex - firstDayOfWeek - daysInMonth + 1).toLong()
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                date = displayDate,
                                isCurrentMonth = false,
                                isToday = false,
                                isSelected = false,
                                isOverride = false,
                                isOffDay = null,
                                label = null,
                                onClick = {}
                            )
                        }
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val dateStr = date.toString()
                        val holidayInfo = holidayMap[dateStr]
                        val overrideInfo = overridesMap[dateStr]

                        val isOverride = overrideInfo != null && overrideInfo.overrideType != 0
                        val isOffDay = when {
                            isOverride -> overrideInfo!!.overrideType == 2
                            holidayInfo != null -> holidayInfo.isOffDay
                            else -> null
                        }

                        val label = when {
                            overrideInfo != null && overrideInfo.overrideType == 1 -> "强班"
                            overrideInfo != null && overrideInfo.overrideType == 2 -> "强休"
                            holidayInfo != null && holidayInfo.isOffDay -> "休"
                            holidayInfo != null && !holidayInfo.isOffDay -> "班"
                            else -> null
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                date = date,
                                isCurrentMonth = true,
                                isToday = date == today,
                                isSelected = selectedDates.contains(date),
                                isOverride = isOverride,
                                isOffDay = isOffDay,
                                label = label,
                                onClick = onDayClick
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val holidayBg = if (isDark) holidayRedDark else holidayRedLight
    val holidayText = if (isDark) holidayRedTextDark else holidayRed
    
    val workdayBg = if (isDark) workdayGreenDark else workdayGreenLight
    val workdayText = if (isDark) workdayGreenTextDark else workdayGreen

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendChip(
                color = holidayText,
                bgColor = holidayBg,
                label = "休 法定假日/强休"
            )
            Spacer(modifier = Modifier.size(16.dp))
            LegendChip(
                color = workdayText,
                bgColor = workdayBg,
                label = "班 调休补班/强班"
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "边框 手动覆盖设定(强制执行)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LegendChip(color: Color, bgColor: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
