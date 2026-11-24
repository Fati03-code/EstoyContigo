package mx.ipn.upiicsa.estoycontigo.domain.model

enum class BreathPhaseType(val label: String) {
    INHALE("Inhala"),
    INHALE_TOP("Inhala un poquito más"),
    HOLD("Sostén"),
    EXHALE("Exhala"),
}

data class BreathPhase(val type: BreathPhaseType, val seconds: Int)

sealed interface InterventionKind {
    data class Breathing(val phases: List<BreathPhase>) : InterventionKind
    data class Guided(val steps: List<String>, val secondsPerStep: Int) : InterventionKind
}

data class Intervention(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val discreet: Boolean,
    val whenToUse: String,
    val kind: InterventionKind,
) {
    val durationLabel: String
        get() = if (durationSeconds < 60) "$durationSeconds s" else "${(durationSeconds + 30) / 60} min"
}
