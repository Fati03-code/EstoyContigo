package mx.ipn.upiicsa.estoycontigo.domain

import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.domain.model.SyncState
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskDetector
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RiskDetectorTest {
    private fun entry(e: Emotion, i: Int) = MoodEntry("id", e, i, "", emptySet(), 0, null, SyncState.PENDING)

    @Test fun detectsCrisisPhrasesIgnoringCaseAndAccents() {
        assertTrue(RiskDetector.noteSuggestsCrisis("Ya NO quiero vivir así"))
        assertTrue(RiskDetector.noteSuggestsCrisis("pienso en el suicidio"))
        assertTrue(RiskDetector.noteSuggestsCrisis("todos estarían mejor sin mí"))
        assertTrue(RiskDetector.noteSuggestsCrisis("Quiero hacerme daño"))
    }

    @Test fun everydayStudentStressIsNotCrisis() {
        assertFalse(RiskDetector.noteSuggestsCrisis("Me muero de sueño, el examen de cálculo me mató"))
        assertFalse(RiskDetector.noteSuggestsCrisis("Reprobé y estoy muy triste"))
        assertFalse(RiskDetector.noteSuggestsCrisis(""))
    }

    @Test fun sustainedDistressNeedsThreeIntenseDifficultOfLastFive() {
        val two = listOf(entry(Emotion.ANSIEDAD, 5), entry(Emotion.TRISTEZA, 4), entry(Emotion.CALMA, 5))
        assertFalse(RiskDetector.hasSustainedDistress(two))
        val three = two + entry(Emotion.ENOJO, 4)
        assertTrue(RiskDetector.hasSustainedDistress(three))
        val lowIntensity = List(5) { entry(Emotion.ANSIEDAD, 3) }
        assertFalse(RiskDetector.hasSustainedDistress(lowIntensity))
        val oldOnes = List(5) { entry(Emotion.ALEGRIA, 2) } + List(3) { entry(Emotion.ANSIEDAD, 5) }
        assertFalse(RiskDetector.hasSustainedDistress(oldOnes))
    }

    @Test fun crisisHasPriorityOverSustainedDistress() {
        val recent = List(5) { entry(Emotion.TRISTEZA, 5) }
        assertEquals(RiskLevel.POSSIBLE_CRISIS, RiskDetector.evaluate("quiero morir", recent))
        assertEquals(RiskLevel.SUSTAINED_DISTRESS, RiskDetector.evaluate("día pesado", recent))
        assertEquals(RiskLevel.NONE, RiskDetector.evaluate("día pesado", emptyList()))
    }
}
