package mx.ipn.upiicsa.estoycontigo.domain.usecase

import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry

enum class RiskLevel { NONE, SUSTAINED_DISTRESS, POSSIBLE_CRISIS }

/**
 * Detección conservadora de señales de riesgo. No diagnostica: solo decide cuándo
 * mostrar de forma proactiva las líneas de ayuda y la orientación de UPIICSA.
 */
object RiskDetector {
    private val crisisPhrases = listOf(
        "suicid", "matarme", "quitarme la vida", "no quiero vivir", "ya no quiero vivir",
        "quiero morir", "quiero morirme", "hacerme daño", "lastimarme", "cortarme",
        "desaparecer para siempre", "no vale la pena vivir", "acabar con todo", "mejor sin mi", "mejor sin mí",
    )

    fun normalize(text: String): String = text.lowercase()
        .replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u')

    fun noteSuggestsCrisis(note: String): Boolean {
        val normalized = normalize(note)
        return crisisPhrases.any { normalized.contains(normalize(it)) }
    }

    /** Tres o más registros difíciles e intensos (>=4) entre los últimos cinco. */
    fun hasSustainedDistress(recentNewestFirst: List<MoodEntry>): Boolean =
        recentNewestFirst.take(5).count { it.emotion.isDifficult && it.intensity >= 4 } >= 3

    fun evaluate(note: String, recentNewestFirst: List<MoodEntry>): RiskLevel = when {
        noteSuggestsCrisis(note) -> RiskLevel.POSSIBLE_CRISIS
        hasSustainedDistress(recentNewestFirst) -> RiskLevel.SUSTAINED_DISTRESS
        else -> RiskLevel.NONE
    }
}
