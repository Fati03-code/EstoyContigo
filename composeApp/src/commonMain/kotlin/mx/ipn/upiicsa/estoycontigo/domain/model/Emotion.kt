package mx.ipn.upiicsa.estoycontigo.domain.model

enum class Emotion(val label: String, val description: String, val isDifficult: Boolean) {
    ALEGRIA("Alegría", "Me siento bien, con energía o contento", false),
    CALMA("Calma", "Tranquilo, en paz, sin prisa", false),
    TRISTEZA("Tristeza", "Desanimado, con ganas de llorar o sin ganas", true),
    ENOJO("Enojo", "Molesto, frustrado o irritable", true),
    ANSIEDAD("Ansiedad", "Preocupado, nervioso o con miedo", true),
    CANSANCIO("Cansancio", "Agotado, sin energía o con sueño", true);

    companion object {
        fun fromKey(key: String): Emotion? = entries.firstOrNull { it.name == key }
    }
}

enum class MoodTag(val label: String) {
    EXAMENES("Exámenes"),
    ENTREGAS("Entregas y proyectos"),
    CLASES("Clases"),
    TRANSPORTE("Transporte"),
    DINERO("Dinero"),
    TRABAJO("Trabajo"),
    FAMILIA("Familia"),
    AMISTADES("Amistades y pareja"),
    SUENO("Sueño"),
    SALUD("Salud"),
    LOGRO("Un logro");

    companion object {
        fun parse(csv: String): Set<MoodTag> =
            csv.split(',').mapNotNull { k -> entries.firstOrNull { it.name == k.trim() } }.toSet()

        fun toCsv(tags: Set<MoodTag>): String = tags.sortedBy { it.ordinal }.joinToString(",") { it.name }
    }
}

object Intensity {
    const val MIN = 1
    const val MAX = 5
    fun label(value: Int): String = when (value) {
        1 -> "Muy leve"
        2 -> "Leve"
        3 -> "Moderada"
        4 -> "Intensa"
        else -> "Muy intensa"
    }
}
