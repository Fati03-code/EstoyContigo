package mx.ipn.upiicsa.estoycontigo.platform

import kotlin.math.sqrt

/**
 * Detecta una sacudida: [requiredPeaks] picos de aceleración > [thresholdG] g en menos de [windowMs].
 * Tiene periodo de enfriamiento para no disparar varias veces seguidas (p. ej. en el metro).
 */
class ShakeDetector(
    private val thresholdG: Float = 2.4f,
    private val requiredPeaks: Int = 3,
    private val windowMs: Long = 900,
    private val cooldownMs: Long = 2_500,
    private val onShake: () -> Unit,
) {
    private val peaks = ArrayDeque<Long>()
    private var lastShakeAt = -cooldownMs
    private var lastPeakAt = Long.MIN_VALUE / 2

    fun onSample(x: Float, y: Float, z: Float, timestampMs: Long) {
        val g = sqrt(x * x + y * y + z * z) / EARTH_GRAVITY
        if (g < thresholdG) return
        if (timestampMs - lastPeakAt < MIN_PEAK_GAP_MS) return
        lastPeakAt = timestampMs
        peaks.addLast(timestampMs)
        while (peaks.isNotEmpty() && timestampMs - peaks.first() > windowMs) peaks.removeFirst()
        if (peaks.size >= requiredPeaks && timestampMs - lastShakeAt > cooldownMs) {
            lastShakeAt = timestampMs
            peaks.clear()
            onShake()
        }
    }

    companion object {
        const val EARTH_GRAVITY = 9.80665f
        const val MIN_PEAK_GAP_MS = 120L
    }
}
