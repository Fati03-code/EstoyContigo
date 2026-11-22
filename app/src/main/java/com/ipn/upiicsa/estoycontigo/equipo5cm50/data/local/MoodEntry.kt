package com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val moodLevel: Int,
    val emotionLabel: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)