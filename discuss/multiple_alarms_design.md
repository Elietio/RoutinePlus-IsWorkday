# 🎨 多闹钟批量判定与状态协同方案设计 (Multi-Channel Alarm Batch Evaluation & State Coordination Scheme)

为了实现用户提出的“可以手动继续添加闹钟，实现一次设置/管理多个闹钟”的重大体验升级，我们对整个应用的底层数据架构、判定引擎和前端 UI 进行了系统性的重构方案设计。

---

## 🏗️ 1. 第一性原理与设计挑战分析

### 1. 三星模式与日常程序的限制 (Routines Limitations)
* **核心局限**：三星日常程序由于系统层安全机制，在拉起静态快捷方式（Shortcuts）时，**无法动态向我们的 `ShortcutActivity` 传入 Intent 额外参数**。
* **挑战**：当多个不同的闹钟在不同的日常程序中触发我们时，如果采用“分路独立判定”模式，系统无法得知是“闹钟 A（7:30）”还是“闹钟 B（8:30）”在请求判定。

### 2. 破局之道：【多路闹钟批量判定与状态协同机制】
* **极简高效流程 (KISS & High-Efficiency)**：
  用户不需要在三星日常程序中为每个闹钟配置单独的触发逻辑，而**只需在三星日常程序中设置一个统一的触发时间点（例如每天晚上睡前 22:00，或每天清晨 05:00）**来拉起我们的快捷方式。
* **协同判定**：
  当我们的 `ShortcutActivity` 被触发时，我们**批量获取数据库中所有已启用的闹钟配置（List of AlarmConfigs）**，并在后台对它们依次独立运行日期判定：
  - 如果该闹钟的触发条件满足（例如：闹钟 A 在工作日生效，而明天是工作日），则**自动在系统闹钟中创建该闹钟**；
  - 如果该闹钟的触发条件不满足（例如：闹钟 A 在工作日生效，而明天是休息日），则**自动跳过**（不创建，从而达到静音/跳过效果）。
* **成倍提效**：
  这种“一键批量评估”的设计完美绕开了三星静态 Shortcuts 无法传参的缺陷。用户只需一次拉起，即可瞬间规划明天一整天的闹钟日程，既优雅又极为鲁棒！

---

## 💾 2. 数据架构设计 (Database & Entity Design)

我们选择使用应用内已有的 **Room 数据库** 来取代原先只能存一条数据的 DataStore 键值对，这为列表的动态增删改查、排序和状态控制提供了最强类型和高并发的保障。

### 1. 新建 `AlarmEntity` 表 (entity 包)
在 `xyz.elietio.routineplus.isworkday.data.local.entity` 中新增：

```kotlin
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetOffset: Int,     // 目标偏移：今天 0 / 明明 1
    val conditionMode: String, // 判定模式：WORKDAY / OFFDAY / ALWAYS
    val hour: Int,             // 小时 (0-23)
    val minute: Int,           // 分钟 (0-59)
    val label: String,         // 闹钟标签
    val isEnabled: Boolean = true // 是否启用开关
)
```

### 2. Room Dao 设计
新建 `AlarmDao.kt` 接口：

```kotlin
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)
}
```

### 3. 数据迁移与老用户平滑兼容 (Backward Compatibility)
为了保障老用户的无缝升级体验：
- **数据库升级**：将 `AppDatabase` 的 version 从 `2` 升为 `3`，并在 App 启动时加入 `MIGRATION_2_3` 自动执行 `CREATE TABLE IF NOT EXISTS alarms...` 的 SQL 语句。
- **数据平滑过渡**：在应用实例化或首次打开时，检查 `alarms` 数据表是否为空。若是，则从原本的 DataStore 数据库读取老用户的“单闹钟配置”，将其转为首条 `AlarmEntity` 存入 Room 中。如此，老用户的历史设定 100% 毫损保留。

---

## ⚡ 3. 判定引擎升级 (Evaluation Engine Upgrade)

### 1. 独立闹钟的幂等保护 (Per-Alarm Anti-Flicker Protection)
因为是批量设置，之前的“全局唯一闹钟缓存机制”会产生冲突。我们需要升级缓存，缓存键应该针对不同的闹钟进行特异性匹配（例如基于 `Alarm ID` 缓存上次设置成功的时间和日期），防止闹钟 B 覆盖闹钟 A 的幂等缓存而导致误跳过。

### 2. 协同控制逻辑
在 `ShortcutActivity` 触发时，进行批量迭代评估：

```kotlin
val enabledAlarms = alarmRepository.getEnabledAlarms()
var setSuccess = 0
var skipped = 0
var failed = 0

for (config in enabledAlarms) {
    val result = setAlarmUseCase(config) // 升级该 UseCase 支持独立的域模型
    if (result.alarmSet) setSuccess++
    else if (!result.shouldSetAlarm) skipped++
    else failed++
}

// 弹出人性化的聚合 Toast 提示 (严格遵守纯粹主义，无 Emoji)：
showToast("闹钟判定完毕：已设置 $setSuccess 个，跳过 $skipped 个")
```

---

## 🎨 4. 前端 UI 重设计 (Material You Multiple Alarm UI)

原本死板的“规则配置”单表单页面，将升级为充满呼吸感、极具 Material 3 美学的 **多闹钟管理器**：

```
+------------------------------------------+
| 规则配置 (标题)                        ⚡  |
+------------------------------------------+
|                                          |
|  ⏰ 07:30  [ 今天 ]                     |
|  通勤闹钟 - 仅工作日                   [o] | (卡片 1 - 莫兰迪薄荷绿)
|                                          |
|  ⏰ 08:30  [ 明天 ]                     |
|  例会备忘 - 仅工作日                   [o] | (卡片 2 - 莫兰迪薄荷绿)
|                                          |
|  ⏰ 09:30  [ 今天 ]                     |
|  周末聚会 - 仅休息日                   [ ] | (卡片 3 - 莫兰迪暗红，关闭状态)
|                                          |
|                                          |
|                                    +---+ |
|                                    | + | | (浮动新增 FAB 按钮)
|                                    +---+ |
+------------------------------------------+
```

### 1. 卡片与列表
- 每一个闹钟都是一个精美的 M3 卡片。已启用的卡片背景使用具有莫兰迪色系的薄荷绿（工作日生效）或放假柔粉（休息日生效），未启用的卡片低调变灰。
- 卡片右侧配备 M3 `Switch` 开关，用户一键无缝开关，无需保存，自动在 Room 数据库中落库。
- 卡片支持点击，一键划出精美的 **编辑 BottomSheet (编辑抽屉)**，支持修改时间、标签、偏移、判定类型，或者删除闹钟。

### 2. 新增闹钟
- 右下角新增悬浮球 **FAB (FloatingActionButton)**。点击后一键弹起 **新增闹钟 BottomSheet**，带来极为饱满、有设计感的 UI 视效。

### 3. 即时测试 ⚡
- 顶栏右上角的“磁盘保存”图标替换为“立即判定测试 (⚡)”图标。用户点按后，即可在后台立刻把当前的所有闹钟执行一遍判定并弹窗反馈，极大方便了用户在不依赖三星日常程序时的手动调试！
