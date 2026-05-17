package xyz.elietio.routineplus.isworkday.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    @PrimaryKey val year: Int,
    val lastSyncTimestamp: Long,
    val dataHash: String
)
