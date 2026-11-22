package com.ipn.upiicsa.estoycontigo.equipo5cm50.data.repository

import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local.MoodDao
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local.MoodEntry
import kotlinx.coroutines.flow.Flow

class MoodRepository(
    private val moodDao: MoodDao
) {
    val allMoods: Flow<List<MoodEntry>> = moodDao.observeAllMoods()

    suspend fun saveMood(moodLevel: Int, emotionLabel: String, note: String) {
        val entry = MoodEntry(
            moodLevel = moodLevel,
            emotionLabel = emotionLabel.trim(),
            note = note.trim(),
            isSynced = false
        )
        moodDao.insertMood(entry)
    }

    suspend fun getUnsyncedMoods(): List<MoodEntry> = moodDao.getUnsyncedMoods()

    suspend fun markMoodsAsSynced(ids: List<Int>) {
        if (ids.isNotEmpty()) {
            moodDao.markAsSynced(ids)
        }
    }
}