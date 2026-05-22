package xyz.elietio.routineplus.isworkday.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val primaryUrl by viewModel.primaryUrl.collectAsState()
    val fallbackUrl by viewModel.fallbackUrl.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saved) {
        if (saved) {
            kotlinx.coroutines.withTimeoutOrNull(1500) {
                snackbarHostState.showSnackbar("设置已保存")
            }
            viewModel.resetSaved()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("全局设置", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() }
                    ) {
                        Text("保存", style = MaterialTheme.typography.labelLarge)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
            // Theme setting (主题外观)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("主题设置", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = "选择应用所呈现的配色外观，支持跟随系统自动变暗",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf("跟随系统", "浅色模式", "深色模式")
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = themeMode == index,
                                onClick = { viewModel.updateThemeMode(index) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            // Database configuration (数据源配置)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("网络数据源", style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                text = "设定法定节假日云端数据的同步端口，应用内建双通道备用容灾机制",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(0.dp)
                    )

                    OutlinedTextField(
                        value = primaryUrl,
                        onValueChange = { viewModel.updatePrimaryUrl(it) },
                        label = { Text("主数据源 (推荐 CDN)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = fallbackUrl,
                        onValueChange = { viewModel.updateFallbackUrl(it) },
                        label = { Text("备用数据源 (防解析异常)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    Text(
                        text = "数据源必须提供兼容的中国法定节假日 JSON 映射，默认经由高速 CDN 代理拉取。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // About (关于)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("关于应用", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("RoutinePlus: IsWorkday", style = MaterialTheme.typography.titleLarge)
                    Text("版本 1.3.0 (M3 Redesign)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "本应用专为三星 Bixby 日常程序 (Modes & Routines) 构建，通过静态 Shortcut 入口无缝拉起高度自定义的“是否工作日”条件逻辑校验，免去用户复杂手动维护的烦恼，实现闹钟智能跳过及日程静音控制。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "默认节假日数据由 NateScarlet/holiday-cn 开源仓库维护，特此致谢。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
