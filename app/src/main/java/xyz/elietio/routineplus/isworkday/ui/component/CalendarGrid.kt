package xyz.elietio.routineplus.isworkday.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import xyz.elietio.routineplus.isworkday.ui.theme.LocalHolidayColorScheme
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
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    // ── Morandi Premium Color Adapters (From Extended Theme Context) ──
    val holidayColors = LocalHolidayColorScheme.current
    val holidayBg = holidayColors.holidayBg
    val holidayText = holidayColors.holidayText
    
    val workdayBg = holidayColors.workdayBg
    val workdayText = holidayColors.workdayText

    // Compute target background color
    val targetBgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        isOffDay == true && isCurrentMonth -> holidayBg
        isOffDay == false && isCurrentMonth -> workdayBg
        else -> Color.Transparent
    }

    // Compute target text color
    val targetTextColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isOffDay == true -> holidayText
        isOffDay == false -> workdayText
        isWeekend -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Animate color changes
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 250),
        label = "dayBgColor"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(durationMillis = 250),
        label = "dayTextColor"
    )

    val bgModifier = if (animatedBgColor != Color.Transparent) {
        Modifier.background(animatedBgColor, CircleShape)
    } else {
        Modifier
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
            .then(bgModifier)     // 1. 先应用背景色以确保其位于底座层 (修复原 bgModifier 覆盖 borderModifier 导致边框隐藏的致命 Bug)
            .then(borderModifier) // 2. 再画边框，使其完美叠浮在日历格子的顶层展示
            .clip(CircleShape)
            .clickable(enabled = isCurrentMonth) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick(date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = animatedTextColor,
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
    val holidayColors = LocalHolidayColorScheme.current
    val holidayBg = holidayColors.holidayBg
    val holidayText = holidayColors.holidayText
    
    val workdayBg = holidayColors.workdayBg
    val workdayText = holidayColors.workdayText

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), CircleShape)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "带外框日期：已手动修改 (强制生效)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
