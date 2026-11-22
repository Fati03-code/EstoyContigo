package com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MoodEntry::class],
    version = 1,
    exportSchema = false
)
abstract class EstoyContigoDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile
        private var INSTANCE: EstoyContigoDatabase? = null

        fun getInstance(context: Context): EstoyContigoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EstoyContigoDatabase::class.java,
                    "estoy_contigo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}