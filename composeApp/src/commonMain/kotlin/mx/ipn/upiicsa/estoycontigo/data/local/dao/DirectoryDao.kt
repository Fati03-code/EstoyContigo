package mx.ipn.upiicsa.estoycontigo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import mx.ipn.upiicsa.estoycontigo.data.local.entity.SupportServiceEntity

@Dao
interface DirectoryDao {
    @Query("SELECT * FROM support_services ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<SupportServiceEntity>>

    @Query("SELECT COUNT(*) FROM support_services")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SupportServiceEntity>)

    @Query("DELETE FROM support_services")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<SupportServiceEntity>) {
        deleteAll()
        insertAll(items)
    }
}
