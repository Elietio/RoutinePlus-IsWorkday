package xyz.elietio.routineplus.isworkday.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.elietio.routineplus.isworkday.data.local.entity.AlarmEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.OverrideEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.SyncMetaEntity

@Database(
    entities = [HolidayEntity::class, SyncMetaEntity::class, OverrideEntity::class, AlarmEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `holiday_overrides` (
                        `date` TEXT NOT NULL, 
                        `overrideType` INTEGER NOT NULL, 
                        `customHour` INTEGER, 
                        `customMinute` INTEGER, 
                        PRIMARY KEY(`date`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `alarms` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `targetOffset` INTEGER NOT NULL, 
                        `conditionMode` TEXT NOT NULL, 
                        `hour` INTEGER NOT NULL, 
                        `minute` INTEGER NOT NULL, 
                        `label` TEXT NOT NULL, 
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `lastAlarmDate` TEXT NOT NULL DEFAULT '',
                        `lastAlarmTimestamp` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
