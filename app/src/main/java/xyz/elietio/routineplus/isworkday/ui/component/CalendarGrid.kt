package xyz.elietio.routineplus.isworkday.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import xyz.elietio.routineplus.isworkday.ui.theme.todayHighlight
import xyz.elietio.routineplus.isworkday.ui.theme.weekendGray
import xyz.elietio.routineplus.isworkday.ui.theme.workdayOrange
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
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        holidayInfo != null && holidayInfo.isOffDay -> holidayRed
        holidayInfo != null && !holidayInfo.isOffDay -> workdayOrange
        isWeekend -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                if (isToday) Modifier.border(2.dp, todayHighlight, CircleShape)
                else Modifier
            )
            .then(
                if (isWeekend && isCurrentMonth && holidayInfo == null)
                    Modifier.background(weekendGray.copy(alpha = 0.5f), CircleShape)
                else Modifier
            )
            .clickable(enabled = isCurrentMonth) { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            if (holidayInfo != null && isCurrentMonth) {
                Text(
                    text = if (holidayInfo.isOffDay) "休" else "班",
                    color = if (holidayInfo.isOffDay) holidayRed else workdayOrange,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
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
            weekDayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Day grid
        var dayCounter = 1
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

        for (week in 0 until totalCells / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dow in 0..6) {
                    val cellIndex = week * 7 + dow
                    if (cellIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                        // Empty or overflow cell
                        val displayDate = if (cellIndex < firstDayOfWeek) {
                            firstDay.minusDays((firstDayOfWeek - cellIndex).toLong())
                        } else {
                            yearMonth.atDay(daysInMonth).plusDays((dayCounter - daysInMonth).toLong())
                                .also { if (cellIndex >= firstDayOfWeek + daysInMonth) dayCounter++ }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                date = if (cellIndex < firstDayOfWeek)
                                    firstDay.minusDays((firstDayOfWeek - cellIndex).toLong())
                                else
                                    yearMonth.atEndOfMonth().plusDays(
                                        (cellIndex - firstDayOfWeek - daysInMonth + 1).toLong()
                                    ),
                                isCurrentMonth = false,
                                isToday = false,
                                holidayInfo = null,
                                onClick = {}
                            )
                        }
                    } else {
                        val date = yearMonth.atDay(dayCounter)
                        val dateStr = date.toString()
                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                date = date,
                                isCurrentMonth = true,
                                isToday = date == today,
                                holidayInfo = holidayMap[dateStr],
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
