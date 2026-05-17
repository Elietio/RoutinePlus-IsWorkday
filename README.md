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

## 🎨 设计语言
本项目全面拥抱 **Google Material You (Material 3)** 规范：
- 全局使用柔和的莫兰迪/马卡龙色系（如薰衣草紫渐变）。
- 提供原生的 Light / Dark 双主题适配。
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
