package xyz.elietio.routineplus.isworkday.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.OverrideEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.SyncMetaEntity

@Database(
    entities = [HolidayEntity::class, SyncMetaEntity::class, OverrideEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao

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
    }
}
