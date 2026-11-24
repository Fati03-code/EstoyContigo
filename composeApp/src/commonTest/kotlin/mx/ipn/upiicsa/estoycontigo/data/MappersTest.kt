package mx.ipn.upiicsa.estoycontigo.data

import mx.ipn.upiicsa.estoycontigo.data.local.entity.MoodEntryEntity
import mx.ipn.upiicsa.estoycontigo.data.local.toDomain
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.domain.model.SyncState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappersTest {
    @Test fun newEntityIsPendingUntilSynced() {
        val e = MoodEntryEntity(emotion = Emotion.ANSIEDAD.name, intensity = 4, note = "examen")
        assertEquals(SyncState.PENDING, e.toDomain().syncState)
        assertEquals(SyncState.SYNCED, e.copy(isSynced = true).toDomain().syncState)
        assertEquals(36, e.id.length)
        assertEquals(e.createdAt, e.updatedAt)
    }

    @Test fun tagsRoundTrip() {
        val tags = setOf(MoodTag.EXAMENES, MoodTag.TRANSPORTE)
        val e = MoodEntryEntity(emotion = Emotion.CALMA.name, tags = MoodTag.toCsv(tags))
        assertEquals(tags, e.toDomain().tags)
        assertTrue(MoodEntryEntity(emotion = "CALMA", tags = "").toDomain().tags.isEmpty())
    }

    @Test fun unknownEmotionFallsBackToCalm() {
        assertEquals(Emotion.CALMA, MoodEntryEntity(emotion = "DESCONOCIDA").toDomain().emotion)
    }
}
