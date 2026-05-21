package xyz.elietio.routineplus.isworkday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_overrides")
data class OverrideEntity(
    @PrimaryKey val date: String,
    val overrideType: Int, // 0 = 跟随法定规则, 1 = 强制设定闹钟, 2 = 强制忽略闹钟
    val customHour: Int? = null,
    val customMinute: Int? = null
)
