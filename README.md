# RoutinePlus: IsWorkday

**RoutinePlus: IsWorkday** 是专为三星“模式与日常程序”设计的系统级逻辑插件。

## 🌟 核心理念
- **精准调度**：基于中国法定节假日与调休规则，提供精准的闹钟自动化调度。
- **本地优先 (Local-First)**：以本地 Room 数据库为核心驱动，弱化网络依赖，确保离线环境下的毫秒级响应。
- **无感集成**：通过静态 Shortcut 完美集成到三星“日常程序”中。无需忍受动态 Shortcut 带来的兼容性问题。

## 🎯 运行机制与工作流
1. **App 内配置**：用户在 App 内配置“判定目标（今天/明天）”、“触发条件（工作日/休息日/每天）”和“闹钟时间”。所有的配置实时保存到本地的 DataStore 中。
2. **日常程序触发**：用户在三星“日常程序”中设定触发条件（如：每晚 22:00），并添加“RoutinePlus”提供的静态快捷方式「执行闹钟规则」。
3. **静默执行**：触发时，应用后台会读取 DataStore 的配置信息。如果是工作日，则自动调用系统底层 API (`AlarmClock.ACTION_SET_ALARM`) 设置系统闹钟，全程无 UI 打扰。

## 📱 三星模式与日常程序 配置指南

要让 **RoutinePlus: IsWorkday** 真正为你服务，你需要配合三星系统的“模式与日常程序”进行以下几步设置：

1. **在 RoutinePlus 内配置规则**：
   - 打开 RoutinePlus，设置好你需要判断的「校验目标」（今天 / 明天）。比如如果你是睡前（如 22:00）运行日常程序，选择“明天”；如果是早上（如 07:00）运行，选择“今天”。
   - 配置「判定条件」（仅工作日 / 仅休息日 / 每天）。
   - 配置你希望设定的「闹钟时间」和「闹钟标签」。
   - 点击“保存配置”（设置会自动持久化保存）。

2. **在系统中创建“日常程序”**：
   - 打开三星手机的 **设置 -> 模式与日常程序 -> 日常程序**。
   - 点击右上角的 `+` 新建一个日常程序。
   - **满足以下条件时 (If)**：设定你的触发时机。例如：“特定时间 -> 晚上 22:30”。
   - **则执行该操作 (Then)**：点击添加操作，选择 `应用程序 -> RoutinePlus -> 执行闹钟规则`（这是一个静态快捷方式）。

3. **效果演示**：
   - 到达晚上 22:30 时，系统会静默唤起 RoutinePlus 的无界面快捷任务。
   - 应用读取本地的法定节假日与调休数据。如果判定符合条件（例如：明天是工作日），则自动在系统时钟里为你设定好闹钟，并在屏幕下方弹出一个纯文本 Toast 提示（如：“明天是工作日，已设置 08:30 的闹钟”），全程无感知、零打扰。

## 🎨 设计语言
本项目全面拥抱 **Google Material You (Material 3)** 规范：
- 全局使用柔和的莫兰迪/马卡龙色系（使用 `secondaryContainer` 替代死板的灰色）。
- 提供原生的 Light / Dark / 跟随系统 主题无缝切换。
- 极简、清爽的 UI 结构与排版。

## 🛠 技术栈
- **语言**：Kotlin 2.1.0
- **UI 框架**：Jetpack Compose
- **依赖注入**：Hilt
- **持久化**：Room (节假日数据), DataStore (偏好配置)
- **网络请求**：Ktor Client
- **后台任务**：WorkManager (12小时周期自动同步节假日数据)
- **CI/CD**：GitHub Actions 自动构建与发布

## 📡 数据源与高可用
节假日数据来自开源项目：[NateScarlet/holiday-cn](https://github.com/NateScarlet/holiday-cn)

为防止单一 CDN 挂掉，应用内置了双链路容灾策略：
- **主链路**：`https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master/`
- **备链路**：`https://fastly.jsdelivr.net/gh/NateScarlet/holiday-cn@master/`

## 📦 如何编译
在项目根目录运行：
```bash
./gradlew assembleDebug
```
Release 版本可以通过推送 `v*.*.*` 标签触发 GitHub Actions 自动构建打包。
