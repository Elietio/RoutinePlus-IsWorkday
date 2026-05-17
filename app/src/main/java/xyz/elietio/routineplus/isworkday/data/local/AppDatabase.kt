package xyz.elietio.routineplus.isworkday.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.elietio.routineplus.isworkday.data.local.entity.HolidayEntity
import xyz.elietio.routineplus.isworkday.data.local.entity.SyncMetaEntity

@Database(
    entities = [HolidayEntity::class, SyncMetaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao
}
