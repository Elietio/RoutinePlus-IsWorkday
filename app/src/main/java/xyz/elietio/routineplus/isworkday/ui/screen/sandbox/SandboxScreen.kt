package xyz.elietio.routineplus.isworkday.ui.screen.sandbox

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    if (showRealTestConfirm) {
        AlertDialog(
            onDismissRequest = { showRealTestConfirm = false },
            title = { Text("确认真实测试") },
            text = { Text("这将使用当前配置创建一个真实的系统闹钟，确定继续？") },
            confirmButton = {
                TextButton(onClick = {
                    showRealTestConfirm = false
                    viewModel.runRealTest(AlarmConfig(targetOffset, conditionMode, 8, 30, "测试闹钟"))
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showRealTestConfirm = false }) { Text("取消") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("沙盒测试", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { viewModel.clearTerminal() }) {
                Icon(Icons.Default.ClearAll, "清空")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Parameters
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Simulated date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("模拟日期: ${simulatedDate?.toString() ?: "当前日期"}")
                    TextButton(onClick = { showDatePicker = true }) { Text("选择") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target offset
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = targetOffset == 0,
                        onClick = { targetOffset = 0 },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("今天") }
                    SegmentedButton(
                        selected = targetOffset == 1,
                        onClick = { targetOffset = 1 },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("明天") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Condition
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ConditionMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = conditionMode == mode,
                            onClick = { conditionMode = mode },
                            shape = SegmentedButtonDefaults.itemShape(index, ConditionMode.entries.size)
                        ) {
                            Text(
                                when (mode) {
                                    ConditionMode.WORKDAY -> "工作日"
                                    ConditionMode.OFFDAY -> "休息日"
                                    ConditionMode.ALWAYS -> "每天"
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.runSimulation(simulatedDate, targetOffset, conditionMode, 8, 30, "通勤闹钟")
                },
                enabled = !isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Text(" 模拟运行")
            }
            OutlinedButton(
                onClick = { showRealTestConfirm = true },
                enabled = !isRunning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.BugReport, null)
                Text(" 真实测试")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SandboxTerminal(lines = lines)
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
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
