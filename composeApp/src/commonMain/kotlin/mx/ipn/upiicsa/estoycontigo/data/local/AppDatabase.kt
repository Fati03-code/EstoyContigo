package mx.ipn.upiicsa.estoycontigo.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import mx.ipn.upiicsa.estoycontigo.data.local.dao.DirectoryDao
import mx.ipn.upiicsa.estoycontigo.data.local.dao.MoodDao
import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity
import mx.ipn.upiicsa.estoycontigo.data.local.entity.SupportServiceEntity

@Database(entities = [MoodEntryEntity::class, SupportServiceEntity::class], version = 1, exportSchema = true)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun directoryDao(): DirectoryDao

    companion object {
        const val FILE_NAME = "estoycontigo.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
