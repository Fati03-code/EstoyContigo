package mx.ipn.upiicsa.estoycontigo.domain.usecase

import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhase
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType.EXHALE
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType.HOLD
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType.INHALE
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType.INHALE_TOP
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.Intervention
import mx.ipn.upiicsa.estoycontigo.domain.model.InterventionKind

/** Micro-intervenciones de 1 a 3 minutos pensadas para hacerse entre clases o en el transporte. */
object InterventionCatalog {
    val sigh = Intervention(
        id = "suspiro",
        title = "Suspiro fisiológico",
        subtitle = "Baja la tensión rápido",
        durationSeconds = 60,
        discreet = true,
        whenToUse = "Antes de un examen, una exposición o cuando sientes el pecho apretado.",
        kind = InterventionKind.Breathing(
            listOf(BreathPhase(INHALE, 2), BreathPhase(INHALE_TOP, 1), BreathPhase(EXHALE, 6)),
        ),
    )
    val box = Intervention(
        id = "cuadrada",
        title = "Respiración cuadrada",
        subtitle = "4 · 4 · 4 · 4 para enfocarte",
        durationSeconds = 64,
        discreet = true,
        whenToUse = "En el metro o el camión, o cuando te cuesta concentrarte.",
        kind = InterventionKind.Breathing(
            listOf(BreathPhase(INHALE, 4), BreathPhase(HOLD, 4), BreathPhase(EXHALE, 4), BreathPhase(HOLD, 4)),
        ),
    )
    val fourSevenEight = Intervention(
        id = "478",
        title = "Respiración 4-7-8",
        subtitle = "Calma la ansiedad",
        durationSeconds = 76,
        discreet = true,
        whenToUse = "Cuando estás preocupado o te cuesta dormir después de estudiar.",
        kind = InterventionKind.Breathing(
            listOf(BreathPhase(INHALE, 4), BreathPhase(HOLD, 7), BreathPhase(EXHALE, 8)),
        ),
    )
    val coherent = Intervention(
        id = "coherente",
        title = "Respiración coherente",
        subtitle = "5 s y 5 s, ritmo tranquilo",
        durationSeconds = 180,
        discreet = true,
        whenToUse = "En un hueco entre clases para recargar energía.",
        kind = InterventionKind.Breathing(listOf(BreathPhase(INHALE, 5), BreathPhase(EXHALE, 5))),
    )
    val grounding = Intervention(
        id = "grounding",
        title = "Anclaje 5-4-3-2-1",
        subtitle = "Vuelve al presente con tus sentidos",
        durationSeconds = 120,
        discreet = true,
        whenToUse = "Si sientes que la mente va muy rápido o te abruma el lugar donde estás.",
        kind = InterventionKind.Guided(
            listOf(
                "Mira a tu alrededor y nombra en silencio 5 cosas que puedes ver.",
                "Nota 4 cosas que puedes sentir: tu ropa, el asiento, tus pies en el piso…",
                "Escucha 3 sonidos distintos, cercanos o lejanos.",
                "Identifica 2 olores. Si no hay, recuerda dos que te gusten.",
                "Reconoce 1 cosa buena de hoy, por pequeña que sea.",
            ),
            secondsPerStep = 24,
        ),
    )
    val bodyReset = Intervention(
        id = "cuerpo",
        title = "Suelta hombros y mandíbula",
        subtitle = "Descarga la tensión de estar sentado",
        durationSeconds = 60,
        discreet = true,
        whenToUse = "Después de horas en clase, en la compu o frente al cuaderno.",
        kind = InterventionKind.Guided(
            listOf(
                "Sube los hombros hacia las orejas… sostén 3 segundos… y déjalos caer.",
                "Separa un poco los dientes y relaja la mandíbula y la lengua.",
                "Suaviza la frente y el espacio entre las cejas.",
                "Apoya la espalda y siente cómo el asiento te sostiene. Lo estás haciendo bien.",
            ),
            secondsPerStep = 15,
        ),
    )
    val selfCompassion = Intervention(
        id = "compasion",
        title = "Háblate como a un amigo",
        subtitle = "Para cuando algo no salió como esperabas",
        durationSeconds = 90,
        discreet = true,
        whenToUse = "Después de una mala calificación, un error o un día pesado.",
        kind = InterventionKind.Guided(
            listOf(
                "Pon una mano en tu pecho si te sientes cómodo y respira lento.",
                "Reconoce lo que pasa: «Esto es difícil para mí ahora».",
                "Recuerda que muchos estudiantes viven algo parecido: no estás solo.",
                "Pregúntate: ¿qué le diría a un amigo en mi lugar? Dítelo a ti.",
                "Elige un paso pequeño para hoy: comer algo, descansar, pedir ayuda.",
            ),
            secondsPerStep = 18,
        ),
    )

    val all: List<Intervention> = listOf(sigh, box, fourSevenEight, grounding, bodyReset, selfCompassion, coherent)

    fun byId(id: String): Intervention? = all.firstOrNull { it.id == id }

    fun recommendedFor(emotion: Emotion?): Intervention = when (emotion) {
        Emotion.ANSIEDAD -> fourSevenEight
        Emotion.ENOJO -> sigh
        Emotion.TRISTEZA -> selfCompassion
        Emotion.CANSANCIO -> bodyReset
        Emotion.ALEGRIA, Emotion.CALMA -> coherent
        null -> box
    }
}
