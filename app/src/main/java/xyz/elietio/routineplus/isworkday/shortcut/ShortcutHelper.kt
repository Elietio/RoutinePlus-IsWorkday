package xyz.elietio.routineplus.isworkday.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.elietio.routineplus.isworkday.R
import xyz.elietio.routineplus.isworkday.domain.model.AlarmConfig
import xyz.elietio.routineplus.isworkday.domain.model.ConditionMode
import xyz.elietio.routineplus.isworkday.receiver.ShortcutReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun publishAlarmShortcut(config: AlarmConfig) {
        val intent = Intent(ShortcutReceiver.ACTION_EXECUTE).apply {
            setPackage(context.packageName)
            putExtra("target_offset", config.targetOffset)
            putExtra("condition_mode", config.conditionMode.name)
            putExtra("alarm_hour", config.hour)
            putExtra("alarm_minute", config.minute)
            putExtra("alarm_label", config.label)
            putExtra("skip_ui", config.skipUi)
        }

        val condLabel = when (config.conditionMode) {
            ConditionMode.WORKDAY -> "工作日"
            ConditionMode.OFFDAY -> "休息日"
            ConditionMode.ALWAYS -> "每天"
        }
        val offsetLabel = if (config.targetOffset == 0) "今天" else "明天"
        val timeStr = "${config.hour}:${config.minute.toString().padStart(2, '0')}"

        val shortcut = ShortcutInfoCompat.Builder(context, "alarm_${config.hashCode()}")
            .setShortLabel("$condLabel $timeStr")
            .setLongLabel("${offsetLabel}${condLabel} $timeStr ${config.label}")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    fun publishSyncShortcut() {
        val intent = Intent(ShortcutReceiver.ACTION_SYNC).apply {
            setPackage(context.packageName)
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "sync_data")
            .setShortLabel("同步节假日")
            .setLongLabel("同步节假日数据")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
}
