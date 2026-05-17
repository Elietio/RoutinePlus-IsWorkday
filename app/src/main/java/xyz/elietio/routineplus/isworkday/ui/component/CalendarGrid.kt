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
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRed
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRedDark
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRedLight
import xyz.elietio.routineplus.isworkday.ui.theme.todayHighlight
import xyz.elietio.routineplus.isworkday.ui.theme.workdayOrange
import xyz.elietio.routineplus.isworkday.ui.theme.workdayOrangeDark
import xyz.elietio.routineplus.isworkday.ui.theme.workdayOrangeLight
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    holidayInfo: HolidayEntity?,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        isToday -> MaterialTheme.colorScheme.onPrimary
        holidayInfo != null && holidayInfo.isOffDay -> holidayRed
        holidayInfo != null && !holidayInfo.isOffDay -> workdayOrange
        isWeekend -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val bgModifier = when {
        isToday -> Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
        holidayInfo != null && holidayInfo.isOffDay && isCurrentMonth ->
            Modifier.background(if (isDark) holidayRedDark else holidayRedLight, CircleShape)
        holidayInfo != null && !holidayInfo.isOffDay && isCurrentMonth ->
            Modifier.background(if (isDark) workdayOrangeDark else workdayOrangeLight, CircleShape)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(bgModifier)
            .clickable(enabled = isCurrentMonth) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            if (holidayInfo != null && isCurrentMonth) {
                Text(
                    text = if (holidayInfo.isOffDay) "休" else "班",
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else if (holidayInfo.isOffDay) holidayRed else workdayOrange,
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
                                holidayInfo = null,
                                onClick = {}
                            )
                        }
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                date = date,
                                isCurrentMonth = true,
                                isToday = date == today,
                                holidayInfo = holidayMap[date.toString()],
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendChip(
            color = holidayRed,
            bgColor = if (isDark) holidayRedDark else holidayRedLight,
            label = "休 法定假日"
        )
        Spacer(modifier = Modifier.size(16.dp))
        LegendChip(
            color = workdayOrange,
            bgColor = if (isDark) workdayOrangeDark else workdayOrangeLight,
            label = "班 调休补班"
        )
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
