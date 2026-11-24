package mx.ipn.upiicsa.estoycontigo.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class ShakeDetectorTest {
    private val strong = 3.0f * ShakeDetector.EARTH_GRAVITY

    @Test fun threePeaksWithinWindowTriggerOneShake() {
        var count = 0
        val d = ShakeDetector { count++ }
        d.onSample(strong, 0f, 0f, 1_000)
        d.onSample(0f, strong, 0f, 1_200)
        d.onSample(0f, 0f, strong, 1_400)
        assertEquals(1, count)
    }

    @Test fun normalMovementAndGravityDoNotTrigger() {
        var count = 0
        val d = ShakeDetector { count++ }
        repeat(50) { i -> d.onSample(0f, ShakeDetector.EARTH_GRAVITY * 1.5f, 0f, 1_000L + i * 20) }
        assertEquals(0, count)
    }

    @Test fun slowPeaksOutsideWindowDoNotTrigger() {
        var count = 0
        val d = ShakeDetector { count++ }
        d.onSample(strong, 0f, 0f, 1_000)
        d.onSample(strong, 0f, 0f, 2_000)
        d.onSample(strong, 0f, 0f, 3_000)
        assertEquals(0, count)
    }

    @Test fun cooldownPreventsRepeatedTriggers() {
        var count = 0
        val d = ShakeDetector { count++ }
        listOf(1_000L, 1_200, 1_400, 1_600, 1_800, 2_000).forEach { d.onSample(strong, 0f, 0f, it) }
        assertEquals(1, count)
        listOf(5_000L, 5_200, 5_400).forEach { d.onSample(strong, 0f, 0f, it) }
        assertEquals(2, count)
    }
}
