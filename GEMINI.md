# 项目级 AI 协作规范 (RoutinePlus: IsWorkday)

本项目为 Android Kotlin Compose 应用，专注于自动化逻辑调度。当 AI (Gemini) 参与本项目开发时，除了遵循全局 `GEMINI.md` 的规范外，还必须严格遵守以下项目级规范。

## 1. UI 设计与美学规范 (Material You)
- **强制使用 Material 3**：所有组件必须使用 `androidx.compose.material3.*`。
- **色彩管理与卡片样式**：使用 `MaterialTheme.colorScheme` 中提供的语义色。对于 Card 组件，避免使用死板的灰色（如 `surfaceContainer`），应当优先使用带有莫兰迪/马卡龙色系倾向的色彩（如 `secondaryContainer` 或 `primaryContainer`）以提升界面的生动性与层次感。绝对禁止在业务代码中硬编码 HEX 颜色。
- **主题适配**：App 必须同时支持浅色、深色模式，并提供“跟随系统 / 浅色 / 深色”的手动切换开关（保存在 DataStore 中，由 MainActivity 注入应用）。
- **纯粹主义**：所有 Toast 和 Terminal 输出必须为**纯文本**，**禁止使用任何 Emoji (如 ✅, 🚫, ⚠️) 或特殊排版符号 (如 `R+ |`)**。

## 2. 三星日常程序集成规范
- **禁止动态快捷方式**：三星“模式与日常程序”对动态 Shortcut 的支持存在兼容性问题。我们**只使用静态 Shortcut** (定义在 `shortcuts.xml` 中)。
- **参数传递策略**：静态 Shortcut 无法动态传递 Intent Extra 参数。所有用户配置的执行参数（如闹钟时间、判断条件等）必须保存在 **DataStore (`ConfigRepository`)** 中。Shortcut 被触发后，由 `ShortcutActivity` 异步读取 DataStore 获取配置并执行逻辑。
- **透明执行**：快捷方式的入口为 `ShortcutActivity`，该 Activity 必须使用透明主题 (`Theme.RoutinePlus.Transparent`) 且在执行完毕后立刻 `finish()`，做到对用户的零视觉打扰。

## 3. 日期计算与时区
- **强制时区**：由于节假日基于北京时间判定，所有涉及到日期转换的地方，必须强制锚定 `Asia/Shanghai` 时区，绝对禁止使用设备默认时区 (`ZoneId.systemDefault()`)，以防止用户在国外旅游时因时差导致节假日判定错乱。

## 4. 网络与数据抓取
- **不可直连 GitHub Raw**：必须通过 CDN（如 JSDelivr 或 Fastly）访问开源节假日数据仓库，并维持“主+备”的双通道容灾设计。
- **离线优先**：读取节假日状态时，一律从 Room 数据库读取，不得在触发时发起网络请求。数据同步由 `WorkManager` 在后台定期默默完成。
