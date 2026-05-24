package xyz.elietio.routineplus.isworkday.ui.screen.config

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val context = LocalContext.current

    // 统一的 Toast 事件监听
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    var activeBottomSheetConfig by remember { mutableStateOf<AlarmConfig?>(null) }
    var showAddBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("规则配置", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(
                        onClick = { viewModel.testTriggerAll() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "手动运行测试"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            val canAdd = alarms.size < 5
            FloatingActionButton(
                onClick = {
                    if (canAdd) {
                        showAddBottomSheet = true
                    } else {
                        Toast.makeText(context, "闹钟数量已达上限 (最多 5 个)", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = if (canAdd) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (canAdd) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加闹钟"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            // 精美的空页面提示
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "未设置任何判定闹钟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请点击右下角按钮添加第一个闹钟配置，您最多可以创建 5 个独立闹钟规则。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { config ->
                    AlarmCard(
                        config = config,
                        onClick = { activeBottomSheetConfig = config },
                        onToggleEnabled = { isEnabled ->
                            viewModel.toggleAlarmEnabled(config, isEnabled)
                        }
                    )
                }
            }
        }
    }

    // 新增闹钟 BottomSheet
    if (showAddBottomSheet) {
        AlarmEditBottomSheet(
            config = AlarmConfig(label = "通勤闹钟"),
            onDismiss = { showAddBottomSheet = false },
            onSave = { newConfig ->
                viewModel.addAlarm(newConfig)
                showAddBottomSheet = false
            },
            isNewAlarm = true
        )
    }

    // 编辑闹钟 BottomSheet
    activeBottomSheetConfig?.let { config ->
        AlarmEditBottomSheet(
            config = config,
            onDismiss = { activeBottomSheetConfig = null },
            onSave = { updatedConfig ->
                viewModel.updateAlarm(updatedConfig)
                activeBottomSheetConfig = null
            },
            onDelete = {
                viewModel.deleteAlarm(config)
                activeBottomSheetConfig = null
            },
            isNewAlarm = false
        )
    }
}

@Composable
fun AlarmCard(
    config: AlarmConfig,
    onClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    // 莫兰迪/马卡龙双态智能着色
    val containerColor = if (config.isEnabled) {
        when (config.conditionMode) {
            ConditionMode.WORKDAY -> MaterialTheme.colorScheme.secondaryContainer
            ConditionMode.OFFDAY -> MaterialTheme.colorScheme.tertiaryContainer
            ConditionMode.ALWAYS -> MaterialTheme.colorScheme.surfaceVariant
        }
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (config.isEnabled) {
        when (config.conditionMode) {
            ConditionMode.WORKDAY -> MaterialTheme.colorScheme.onSecondaryContainer
            ConditionMode.OFFDAY -> MaterialTheme.colorScheme.onTertiaryContainer
            ConditionMode.ALWAYS -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val cardAlpha = if (config.isEnabled) 1.0f else 0.5f

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = CardDefaults.outlinedCardBorder(enabled = config.isEnabled)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 时间显示 (大排版)
                Text(
                    text = "${config.hour}:${config.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                // 闹钟标签
                Text(
                    text = config.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.9f)
                )

                // 规则描述小胶囊
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offsetText = if (config.targetOffset == 0) "今天判定" else "明天判定"
                    val ruleText = when (config.conditionMode) {
                        ConditionMode.WORKDAY -> "仅工作日生效"
                        ConditionMode.OFFDAY -> "仅休息日生效"
                        ConditionMode.ALWAYS -> "每天生效"
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text(offsetText, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = contentColor.copy(alpha = 0.8f)
                        ),
                        border = null
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text(ruleText, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = contentColor.copy(alpha = 0.8f)
                        ),
                        border = null
                    )
                }
            }

            // M3 Switch 独立开关
            Switch(
                checked = config.isEnabled,
                onCheckedChange = { onToggleEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditBottomSheet(
    config: AlarmConfig,
    onDismiss: () -> Unit,
    onSave: (AlarmConfig) -> Unit,
    onDelete: (() -> Unit)? = null,
    isNewAlarm: Boolean
) {
    var hour by remember { mutableStateOf(config.hour) }
    var minute by remember { mutableStateOf(config.minute) }
    var label by remember { mutableStateOf(config.label) }
    var targetOffset by remember { mutableStateOf(config.targetOffset) }
    var conditionMode by remember { mutableStateOf(config.conditionMode) }

    var showTimePickerDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isNewAlarm) "新增闹钟配置" else "编辑闹钟配置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                }
            }

            // 1. 时间选择卡片
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("闹钟设定时间", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${hour}:${minute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Button(
                        onClick = { showTimePickerDialog = true }
                    ) {
                        Text("修改时间")
                    }
                }
            }

            // 2. 校验目标设定 (Segmented Button Row)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("判定目标日期", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = targetOffset == 0,
                        onClick = { targetOffset = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("今天 Today") }
                    SegmentedButton(
                        selected = targetOffset == 1,
                        onClick = { targetOffset = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("明天 Tomorrow") }
                }
            }

            // 3. 判定条件 (Segmented Button Row)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("判定触发条件", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ConditionMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = conditionMode == mode,
                            onClick = { conditionMode = mode },
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

            // 4. 闹钟标签
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("闹钟标签") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. 底部保存与删除动作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isNewAlarm && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("删除闹钟")
                    }
                }

                Button(
                    onClick = {
                        onSave(
                            config.copy(
                                hour = hour,
                                minute = minute,
                                label = label,
                                targetOffset = targetOffset,
                                conditionMode = conditionMode
                            )
                        )
                    },
                    modifier = Modifier.weight(if (isNewAlarm) 1f else 1.5f)
                ) {
                    Text(if (isNewAlarm) "创建闹钟" else "确定修改")
                }
            }
        }
    }

    // TimePicker 弹窗
    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
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
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePickerDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) { Text("取消") }
            },
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        )
    }
}
