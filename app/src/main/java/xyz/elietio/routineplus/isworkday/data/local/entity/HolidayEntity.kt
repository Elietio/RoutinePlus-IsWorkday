package xyz.elietio.routineplus.isworkday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_days")
data class HolidayEntity(
    @PrimaryKey val date: String,
    val name: String,
    val isOffDay: Boolean,
    val year: Int
)
