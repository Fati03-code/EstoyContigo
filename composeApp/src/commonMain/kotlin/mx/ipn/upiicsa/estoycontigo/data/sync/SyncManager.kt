package mx.ipn.upiicsa.estoycontigo.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.ipn.upiicsa.estoycontigo.data.local.dao.MoodDao
import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.remote.FirestoreApi
import mx.ipn.upiicsa.estoycontigo.data.remote.StorageApi
import mx.ipn.upiicsa.estoycontigo.data.remote.dto.MoodDocument
import mx.ipn.upiicsa.estoycontigo.data.repository.AuthRepository
import mx.ipn.upiicsa.estoycontigo.platform.PhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.nowMillis

sealed interface SyncResult {
    data class Success(val uploaded: Int, val deleted: Int) : SyncResult
    data class Partial(val uploaded: Int, val failed: Int) : SyncResult
    data class Skipped(val reason: String) : SyncResult
}

/**
 * Cola Queue & Sync: sube a Firestore/Storage los registros con isSynced = false
 * y los marca como sincronizados (verde). Es idempotente: se puede llamar varias veces.
 */
class SyncManager(
    private val dao: MoodDao,
    private val auth: AuthRepository,
    private val firestore: FirestoreApi,
    private val storage: StorageApi,
    private val photos: PhotoStorage,
    private val prefs: PreferencesStore,
) {
    private val mutex = Mutex()

    suspend fun syncPending(): SyncResult = mutex.withLock {
        if (!auth.isCloudAvailable) return SyncResult.Skipped("Nube no configurada")
        val session = auth.validSession() ?: return SyncResult.Skipped("Sin sesión")
        val pending = dao.getUnsynced()
        var uploaded = 0
        var deleted = 0
        var failed = 0
        for (entry in pending) {
            try {
                if (entry.isDeleted) {
                    firestore.deleteMood(session.uid, session.idToken, entry.id)
                    entry.photoRemotePath?.let { storage.deletePhoto(session.idToken, it) }
                    dao.deleteById(entry.id)
                    deleted++
                } else {
                    val remotePhoto = uploadPhotoIfNeeded(entry, session.uid, session.idToken)
                    firestore.upsertMood(session.uid, session.idToken, entry.toDocument(remotePhoto))
                    val marked = dao.markSynced(entry.id, remotePhoto, entry.updatedAt)
                    if (marked == 0 && dao.getById(entry.id) == null) {
                        // Se borró localmente mientras se subía: revertir la copia en la nube.
                        firestore.deleteMood(session.uid, session.idToken, entry.id)
                        remotePhoto?.let { storage.deletePhoto(session.idToken, it) }
                        deleted++
                    } else {
                        uploaded++
                    }
                }
            } catch (e: Exception) {
                failed++
            }
        }
        if (failed == 0) {
            prefs.setLastSyncAt(nowMillis())
            SyncResult.Success(uploaded, deleted)
        } else SyncResult.Partial(uploaded, failed)
    }

    /** Recupera desde la nube los registros que no existen en este dispositivo (p. ej. al cambiar de teléfono). */
    suspend fun restoreFromCloud(): Int = mutex.withLock {
        val session = auth.validSession() ?: return 0
        val localIds = dao.getAllIds().toSet()
        val remote = firestore.listMoods(session.uid, session.idToken).filter { it.id !in localIds }
        val restored = remote.map { doc ->
            val localPhoto = doc.photoPath?.let { path ->
                runCatching { photos.save("${doc.id}.jpg", storage.downloadPhoto(session.idToken, path)) }.getOrNull()
            }
            MoodEntryEntity(
                id = doc.id,
                emotion = doc.emotion,
                intensity = doc.intensity,
                note = doc.note,
                tags = doc.tags,
                createdAt = doc.createdAt,
                updatedAt = doc.updatedAt,
                photoPath = localPhoto,
                photoRemotePath = doc.photoPath,
                isSynced = true,
            )
        }
        dao.upsertAll(restored)
        restored.size
    }

    /** Borra todos los datos del usuario en la nube (derecho de cancelación ARCO). */
    suspend fun deleteAllRemote(): Boolean = mutex.withLock {
        val session = auth.validSession() ?: return false
        val photoPaths = storage.listPhotos(session.idToken, session.uid) +
            firestore.listMoods(session.uid, session.idToken).mapNotNull { it.photoPath }
        photoPaths.toSet().forEach { storage.deletePhoto(session.idToken, it) }
        firestore.listMoods(session.uid, session.idToken).forEach { doc ->
            firestore.deleteMood(session.uid, session.idToken, doc.id)
        }
        auth.deleteRemoteAccount(session.idToken)
        true
    }

    private suspend fun uploadPhotoIfNeeded(entry: MoodEntryEntity, uid: String, token: String): String? {
        entry.photoRemotePath?.let { return it }
        val local = entry.photoPath ?: return null
        val bytes = photos.readBytes(local) ?: return null
        return storage.uploadPhoto(token, storage.photoPath(uid, entry.id), bytes)
    }

    private fun MoodEntryEntity.toDocument(remotePhoto: String?) = MoodDocument(
        id = id,
        emotion = emotion,
        intensity = intensity,
        note = note,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        photoPath = remotePhoto,
    )
}
