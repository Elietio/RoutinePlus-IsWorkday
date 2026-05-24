package xyz.elietio.routineplus.isworkday.ui.screen.sandbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.ui.component.SandboxTerminal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(
    viewModel: SandboxViewModel = hiltViewModel()
) {
    val lines by viewModel.lines.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    var targetOffset by remember { mutableIntStateOf(1) }
    var conditionMode by remember { mutableStateOf(ConditionMode.WORKDAY) }
    var simulatedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRealTestConfirm by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (showRealTestConfirm) {
        AlertDialog(
            onDismissRequest = { showRealTestConfirm = false },
            title = { Text("确认真实测试", style = MaterialTheme.typography.titleMedium) },
            text = { Text("这将使用当前配置在系统闹钟中创建一条真实的验证广播，确定继续？") },
            confirmButton = {
                TextButton(onClick = {
                    showRealTestConfirm = false
                    viewModel.runRealTest(
                        AlarmConfig(
                            targetOffset = targetOffset,
                            conditionMode = conditionMode,
                            hour = 8,
                            minute = 30,
                            label = "测试闹钟"
                        )
                    )
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showRealTestConfirm = false }) { Text("取消") }
            },
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("沙盒测试", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { viewModel.clearTerminal() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "清空终端")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Parameters (模拟配置参数)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Simulated date selection row
                    ListItem(
                        headlineContent = { Text("模拟运行日期", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = "设定仿真环境的时间，为空则默认使用北京时间当前日期",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { showDatePicker = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(simulatedDate?.toString() ?: "当前日期")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Target offset
                    ListItem(
                        headlineContent = { Text("校验偏移 (校验目标)", style = MaterialTheme.typography.titleSmall) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = targetOffset == 0,
                            onClick = { targetOffset = 0 },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("今天 Today") }
                        SegmentedButton(
                            selected = targetOffset == 1,
                            onClick = { targetOffset = 1 },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("明天 Tomorrow") }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Condition
                    ListItem(
                        headlineContent = { Text("判定条件设定", style = MaterialTheme.typography.titleSmall) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ConditionMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = conditionMode == mode,
                                onClick = { conditionMode = mode },
                                shape = SegmentedButtonDefaults.itemShape(index, ConditionMode.entries.size)
                            ) {
                                Text(
                                    when (mode) {
                                        ConditionMode.WORKDAY -> "仅工作日"
                                        ConditionMode.OFFDAY -> "仅休息日"
                                        ConditionMode.ALWAYS -> "每天"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.runSimulation(simulatedDate, targetOffset, conditionMode, 8, 30, "通勤闹钟")
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("模拟运行")
                }
                OutlinedButton(
                    onClick = { showRealTestConfirm = true },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.BugReport, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("真实测试")
                }
            }

            // Sandbox terminal output
            ListItem(
                headlineContent = { Text("控制台终端输出", style = MaterialTheme.typography.titleMedium) },
                supportingContent = { Text("显示每次模拟及实际测试所产生的底层指令追踪日志", style = MaterialTheme.typography.bodySmall) },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                modifier = Modifier.padding(top = 8.dp)
            )

            SandboxTerminal(
                lines = lines,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        simulatedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("Asia/Shanghai"))
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
