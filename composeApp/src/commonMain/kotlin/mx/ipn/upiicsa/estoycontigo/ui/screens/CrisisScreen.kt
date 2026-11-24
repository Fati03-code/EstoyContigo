package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.DirectoryViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Pantalla de crisis: funciona sin internet y sin cuenta. Prioriza el contacto humano inmediato. */
@Composable
fun CrisisScreen(onBack: () -> Unit, onDirectory: () -> Unit, onBreathing: () -> Unit, vm: DirectoryViewModel = koinViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val actions = vm.actions
    val crisis = s.services.filter { it.category == ServiceCategory.CRISIS }
    val upiicsa = s.services.firstOrNull { it.category == ServiceCategory.UPIICSA }

    AppScreen(title = "Ayuda inmediata", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Michi(size = 76.dp, mood = MichiMood.CARING)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Estoy contigo", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    Text("Si sientes que no puedes más, hablar con alguien ahora puede ayudar. No tienes que explicarlo perfecto.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                onClick = { actions.dial("911") },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.extra.crisis, contentColor = AppTheme.extra.onCrisis),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            ) {
                Icon(Icons.Outlined.Call, null); Spacer(Modifier.width(8.dp))
                Text("Emergencia: llamar al 911", style = MaterialTheme.typography.titleMedium)
            }
            Text("Si estás en peligro inmediato o te hiciste daño, llama al 911 o pide a alguien cerca que te acompañe.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Text("Líneas de apoyo emocional (gratuitas y confidenciales)", style = MaterialTheme.typography.titleMedium)
            crisis.forEach { svc ->
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(svc.name, style = MaterialTheme.typography.titleSmall)
                            Text((if (svc.is24h) "24 h · " else "") + (svc.phone ?: ""), style = MaterialTheme.typography.bodySmall)
                        }
                        svc.phone?.let { p ->
                            Button(onClick = { actions.dial(p) }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Llamar") }
                        }
                    }
                }
            }

            if (s.trustedPhone.isNotBlank()) {
                OutlinedButton(onClick = { actions.dial(s.trustedPhone) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Icon(Icons.Outlined.Person, null); Spacer(Modifier.width(8.dp))
                    Text("Llamar a ${s.trustedName.ifBlank { "mi contacto de confianza" }}")
                }
            }

            upiicsa?.let { svc ->
                InfoCard(container = MaterialTheme.colorScheme.primaryContainer) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("En la escuela: ${svc.name}", style = MaterialTheme.typography.titleSmall)
                        Text(svc.schedule, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            svc.phone?.let { p -> Button(onClick = { actions.dial(p) }) { Text("Llamar") } }
                            OutlinedButton(onClick = onDirectory) { Text("Más opciones") }
                        }
                    }
                }
            }

            InfoCard(container = MaterialTheme.colorScheme.surfaceContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Mientras tanto, para este momento", style = MaterialTheme.typography.titleSmall)
                    Text("• Aléjate de cualquier cosa con la que puedas lastimarte.\n" +
                        "• Busca un lugar con más gente o escribe a alguien de confianza.\n" +
                        "• Respira lento: exhala más largo de lo que inhalas.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = onBreathing) { Text("Respirar 1 minuto con Michi") }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
