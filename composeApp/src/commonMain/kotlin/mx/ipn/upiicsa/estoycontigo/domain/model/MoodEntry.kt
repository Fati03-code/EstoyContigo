package mx.ipn.upiicsa.estoycontigo.domain.model

enum class SyncState { PENDING, SYNCED }

data class MoodEntry(
    val id: String,
    val emotion: Emotion,
    val intensity: Int,
    val note: String,
    val tags: Set<MoodTag>,
    val createdAt: Long,
    val photoPath: String?,
    val syncState: SyncState,
)

data class MoodDraft(
    val emotion: Emotion?,
    val intensity: Int,
    val note: String,
    val tags: Set<MoodTag>,
    val photoPath: String?,
)
