package xyz.elietio.routineplus.isworkday.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.elietio.routineplus.isworkday.ui.component.CalendarGrid
import xyz.elietio.routineplus.isworkday.ui.component.CalendarLegend
import xyz.elietio.routineplus.isworkday.ui.component.StatusCard
import xyz.elietio.routineplus.isworkday.ui.theme.holidayRed
import xyz.elietio.routineplus.isworkday.ui.theme.workdayOrange
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    val monthFormatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        StatusCard(
            lastSyncTime = lastSyncTime,
            isSyncing = isSyncing,
            onSyncClick = { viewModel.sync() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上一月")
            }
            Text(
                text = currentMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { viewModel.navigateMonth(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下一月")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            yearMonth = currentMonth,
            holidays = holidays,
            onDayClick = { viewModel.selectDay(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarLegend()
    }

    // Bottom sheet for day details
    if (selectedDay != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = selectedDay!!.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "日期: ${selectedDay!!.date}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (selectedDay!!.isOffDay) "类型: 休息日" else "类型: 补班日",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedDay!!.isOffDay) holidayRed else workdayOrange
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


