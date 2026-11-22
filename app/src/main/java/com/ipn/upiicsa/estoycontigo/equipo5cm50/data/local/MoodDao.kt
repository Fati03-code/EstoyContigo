package com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun observeAllMoods(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE isSynced = 0")
    suspend fun getUnsyncedMoods(): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(entry: MoodEntry)

    @Query("UPDATE mood_entries SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)
}