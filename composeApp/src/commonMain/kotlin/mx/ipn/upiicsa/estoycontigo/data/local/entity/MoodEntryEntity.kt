package mx.ipn.upiicsa.estoycontigo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Entity(tableName = "mood_entries", indices = [Index("createdAt"), Index("isSynced")])
data class MoodEntryEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val emotion: String,
    val intensity: Int = 3,
    val note: String = "",
    val tags: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = createdAt,
    val photoPath: String? = null,
    val photoRemotePath: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)
