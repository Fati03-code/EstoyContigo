package mx.ipn.upiicsa.estoycontigo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE id = :id AND isDeleted = 0")
    fun observeById(id: String): Flow<MoodEntryEntity?>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: String): MoodEntryEntity?

    @Query("SELECT * FROM mood_entries WHERE isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<MoodEntryEntity>

    @Query("SELECT * FROM mood_entries WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsynced(): List<MoodEntryEntity>

    @Query("SELECT COUNT(*) FROM mood_entries WHERE isSynced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT id FROM mood_entries")
    suspend fun getAllIds(): List<String>

    @Upsert
    suspend fun upsert(entry: MoodEntryEntity)

    @Upsert
    suspend fun upsertAll(entries: List<MoodEntryEntity>)

    /** Solo marca como sincronizado si nadie modificó el registro mientras se subía. */
    @Query("UPDATE mood_entries SET isSynced = 1, photoRemotePath = :photoRemotePath WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSynced(id: String, photoRemotePath: String?, updatedAt: Long): Int

    @Query("UPDATE mood_entries SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Long)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}
