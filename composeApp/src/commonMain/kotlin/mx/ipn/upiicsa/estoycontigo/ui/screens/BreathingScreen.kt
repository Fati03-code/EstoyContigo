package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType
import mx.ipn.upiicsa.estoycontigo.domain.model.InterventionKind
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.BreathingViewModel
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.PlayerStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BreathingScreen(id: String, onBack: () -> Unit, onSos: () -> Unit, vm: BreathingViewModel = koinViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val moving by vm.isMoving.collectAsStateWithLifecycle()
    val frame = s.frame
    val running = s.status == PlayerStatus.RUNNING

    // Escala del círculo según la fase (crece al inhalar, se mantiene al sostener y baja al exhalar).
    val target = when (frame.phaseType) {
        BreathPhaseType.INHALE -> 0.6f + 0.35f * frame.phaseProgress
        BreathPhaseType.INHALE_TOP -> 0.95f + 0.05f * frame.phaseProgress
        BreathPhaseType.HOLD -> null
        BreathPhaseType.EXHALE -> 1f - 0.4f * frame.phaseProgress
        null -> 0.8f
    }
    val scale by animateFloatAsState(if (running) target ?: 1f else 0.75f, tween(120), label = "scale")
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer

    AppScreen(title = s.intervention.title, onBack = onBack, onSos = onSos) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(s.intervention.subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(container.copy(alpha = 0.5f), radius = size.minDimension / 2 * scale)
                    drawCircle(container, radius = size.minDimension / 2 * scale * 0.8f)
                }
                Michi(size = 130.dp, mood = if (s.status == PlayerStatus.DONE) MichiMood.HAPPY else MichiMood.CALM, breathScale = 0.9f + scale * 0.12f)
            }

            Text(
                when (s.status) {
                    PlayerStatus.READY -> "Cuando quieras, empezamos"
                    PlayerStatus.DONE -> "Lo lograste. ¿Cómo te sientes ahora?"
                    else -> frame.label
                },
                style = if (s.intervention.kind is InterventionKind.Guided && running) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (running && s.intervention.kind is InterventionKind.Breathing) {
                Text("${frame.phaseSecondsLeft}", style = MaterialTheme.typography.displaySmall, color = primary)
            }

            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth().height(8.dp))
            Text("${s.secondsLeft / 60}:${(s.secondsLeft % 60).toString().padStart(2, '0')} restantes",
                style = MaterialTheme.typography.labelLarge)

            if (moving && running) {
                InfoCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("Parece que vas en movimiento. Mantén los ojos abiertos y sujétate bien; puedes respirar igual.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (s.status) {
                    PlayerStatus.RUNNING -> OutlinedButton(onClick = vm::pause, modifier = Modifier.heightIn(min = 52.dp)) { Text("Pausar") }
                    PlayerStatus.DONE -> {
                        OutlinedButton(onClick = vm::restart, modifier = Modifier.heightIn(min = 52.dp)) { Text("Repetir") }
                        Button(onClick = onBack, modifier = Modifier.heightIn(min = 52.dp)) { Text("Terminar") }
                    }
                    else -> Button(onClick = vm::start, modifier = Modifier.heightIn(min = 52.dp)) {
                        Text(if (s.status == PlayerStatus.PAUSED) "Continuar" else "Empezar · ${s.intervention.durationLabel}")
                    }
                }
            }
            InfoCard {
                Text("Cuándo usarla: ${s.intervention.whenToUse}\n\nSi te mareas, vuelve a respirar normal. " +
                    "Puedes detenerte en cualquier momento.", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
