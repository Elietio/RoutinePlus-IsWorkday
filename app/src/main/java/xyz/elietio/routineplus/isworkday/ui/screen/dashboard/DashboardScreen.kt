package xyz.elietio.routineplus.isworkday.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.elietio.routineplus.isworkday.ui.component.CalendarGrid
import xyz.elietio.routineplus.isworkday.ui.component.CalendarLegend
import java.time.LocalDate
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val holidays by viewModel.holidays.collectAsState()
    val overridesMap by viewModel.overridesMap.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedDates by viewModel.selectedDates.collectAsState()

    val monthFormatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINA)

    val initialMonth = remember { YearMonth.now(java.time.ZoneId.of("Asia/Shanghai")) }
    val totalPages = 240
    val initialPage = 120

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalPages }
    )

    // Month Navigation Click -> Smooth Pager Scroll
    LaunchedEffect(currentMonth) {
        val targetPage = initialPage + ChronoUnit.MONTHS.between(initialMonth, currentMonth).toInt()
        if (pagerState.currentPage != targetPage && targetPage in 0 until totalPages) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Gesture Swipe Pager -> Select Month
    LaunchedEffect(pagerState.currentPage) {
        val targetMonth = initialMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
        if (currentMonth != targetMonth) {
            viewModel.selectMonth(targetMonth)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("IsWorkday", fontWeight = FontWeight.Bold)
                        Text(
                            text = currentMonth.format(monthFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上一月")
                    }
                    IconButton(onClick = { viewModel.navigateMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下一月")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.sync() }) {
                            Icon(
                                imageVector = if (lastSyncTime != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "同步"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Precise Sync Notification Banner (Lightweight M3 Card)
            val currentLastSyncTime = lastSyncTime
            if (currentLastSyncTime != null && currentLastSyncTime > 0L) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(java.time.ZoneId.of("Asia/Shanghai"))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "节假日数据已同步 (上次: ${formatter.format(Instant.ofEpochMilli(currentLastSyncTime))})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pageMonth = initialMonth.plusMonths((page - initialPage).toLong())
                CalendarGrid(
                    yearMonth = pageMonth,
                    holidays = holidays,
                    overridesMap = overridesMap,
                    selectedDates = selectedDates,
                    onDayClick = { viewModel.toggleDateSelection(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CalendarLegend()
        }
    }

    // Bottom sheet for day details and custom configurations
    if (selectedDates.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        var overrideType by remember(selectedDates) {
            val firstDateStr = selectedDates.firstOrNull()?.toString()
            val existing = firstDateStr?.let { overridesMap[it] }
            mutableStateOf(existing?.overrideType ?: 0)
        }

        var useCustomTime by remember(selectedDates) {
            val firstDateStr = selectedDates.firstOrNull()?.toString()
            val existing = firstDateStr?.let { overridesMap[it] }
            mutableStateOf(existing?.customHour != null && existing?.customMinute != null)
        }

        var customHour by remember(selectedDates) {
            val firstDateStr = selectedDates.firstOrNull()?.toString()
            val existing = firstDateStr?.let { overridesMap[it] }
            mutableStateOf(existing?.customHour ?: 8)
        }

        var customMinute by remember(selectedDates) {
            val firstDateStr = selectedDates.firstOrNull()?.toString()
            val existing = firstDateStr?.let { overridesMap[it] }
            mutableStateOf(existing?.customMinute ?: 30)
        }

        var showTimePicker by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedDates() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                val title = if (selectedDates.size == 1) {
                    "设置日期: ${selectedDates.first()}"
                } else {
                    "批量设置: 已选中 ${selectedDates.size} 天"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Override Radio Options
                val options = listOf(
                    0 to "跟随法定规则 (默认)",
                    1 to "强制设定闹钟 (比如临时加班)",
                    2 to "强制忽略闹钟 (比如请假/休假)"
                )

                Column(modifier = Modifier.selectableGroup()) {
                    options.forEach { (typeCode, text) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (overrideType == typeCode),
                                    onClick = { overrideType = typeCode },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (overrideType == typeCode),
                                onClick = { overrideType = typeCode }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Custom Time Configuration (Only when Force Alarm is selected)
                AnimatedVisibility(visible = overrideType == 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "启用特定自定义时间",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Switch(
                                        checked = useCustomTime,
                                        onCheckedChange = { useCustomTime = it }
                                    )
                                }

                                if (useCustomTime) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showTimePicker = true },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "特定日期闹钟时间",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        val timeText = "${customHour.toString().padStart(2, '0')}:${customMinute.toString().padStart(2, '0')}"
                                        SuggestionChip(
                                            onClick = { showTimePicker = true },
                                            label = { Text(timeText) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.clearSelectedDates() }) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            viewModel.applyOverrides(
                                overrideType = overrideType,
                                customHour = if (useCustomTime) customHour else null,
                                customMinute = if (useCustomTime) customMinute else null
                            )
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = customHour,
                initialMinute = customMinute,
                is24Hour = true
            )
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            customHour = timePickerState.hour
                            customMinute = timePickerState.minute
                            showTimePicker = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择时间",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}
