package xyz.elietio.routineplus.isworkday.ui.screen.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    var labelInput by remember(config.label) { mutableStateOf(config.label) }

    LaunchedEffect(labelInput) {
        if (labelInput != config.label) {
            kotlinx.coroutines.delay(400)
            viewModel.updateLabel(labelInput)
        }
    }

    var showTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("规则配置", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                kotlinx.coroutines.withTimeoutOrNull(1500) {
                                    snackbarHostState.showSnackbar("配置已保存")
                                }
                            }
                        }
                    ) {
                        Text("保存", style = MaterialTheme.typography.labelLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
            // Target offset (校验目标)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("校验目标", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = if (config.targetOffset == 0) "\"今天\"适用于跨零点后的凌晨触发"
                                else "\"明天\"适用于睡前触发",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = config.targetOffset == 0,
                            onClick = { viewModel.updateTargetOffset(0) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("今天 Today") }
                        SegmentedButton(
                            selected = config.targetOffset == 1,
                            onClick = { viewModel.updateTargetOffset(1) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("明天 Tomorrow") }
                    }
                }
            }

            // Condition mode (判定条件)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("判定条件", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = "满足选定的判断逻辑时，日常程序才会继续执行",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ConditionMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = config.conditionMode == mode,
                                onClick = { viewModel.updateConditionMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = ConditionMode.entries.size)
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

            // Alarm time (闹钟设定)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("闹钟触发设定", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = "此闹钟仅作为触发容器，在设定时刻拉起本判定逻辑",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${config.hour}:${config.minute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Text("修改时间")
                        }
                    }

                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("闹钟标签") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = config.hour,
            initialMinute = config.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择触发时间", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        )
    }
}
