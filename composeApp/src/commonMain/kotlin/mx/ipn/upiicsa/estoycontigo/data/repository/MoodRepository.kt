package mx.ipn.upiicsa.estoycontigo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.ipn.upiicsa.estoycontigo.data.local.dao.MoodDao
import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity
import mx.ipn.upiicsa.estoycontigo.data.local.toDomain
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodDraft
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.domain.usecase.MoodValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.ValidationResult
import mx.ipn.upiicsa.estoycontigo.platform.PhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.platform.nowMillis

/**
 * Repositorio Offline-First: Room es la única fuente de verdad (SSOT).
 * Todo se escribe primero localmente con isSynced = false y después se agenda la sincronización.
 */
class MoodRepository(
    private val dao: MoodDao,
    private val photoStorage: PhotoStorage,
    private val syncScheduler: SyncScheduler,
) {
    val entries: Flow<List<MoodEntry>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
    val pendingCount: Flow<Int> = dao.observePendingCount()

    fun observeEntry(id: String): Flow<MoodEntry?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun recent(limit: Int = 10): List<MoodEntry> = dao.getRecent(limit).map { it.toDomain() }

    suspend fun save(draft: MoodDraft): Result<MoodEntry> {
        val validation = MoodValidator.validate(draft)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.errors.values.first()))
        }
        val now = nowMillis()
        val entity = MoodEntryEntity(
            emotion = draft.emotion!!.name,
            intensity = draft.intensity,
            note = draft.note.trim(),
            tags = MoodTag.toCsv(draft.tags),
            createdAt = now,
            updatedAt = now,
            photoPath = draft.photoPath,
            isSynced = false,
        )
        dao.upsert(entity)
        syncScheduler.requestSync()
        return Result.success(entity.toDomain())
    }

    suspend fun delete(id: String) {
        val entity = dao.getById(id) ?: return
        entity.photoPath?.let { photoStorage.delete(it) }
        if (!entity.isSynced && entity.photoRemotePath == null) {
            dao.deleteById(id)
        } else {
            dao.markDeleted(id, nowMillis())
            syncScheduler.requestSync()
        }
    }

    suspend fun deleteAllLocal() {
        dao.deleteAll()
        photoStorage.deleteAll()
    }
}
