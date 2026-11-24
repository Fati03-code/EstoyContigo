package mx.ipn.upiicsa.estoycontigo.ui.screens

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

private data class Page(val title: String, val body: String, val mood: MichiMood)

private val pages = listOf(
    Page(
        "Hola, soy Michi",
        "Te acompaño en los días buenos y en los pesados: exámenes, entregas, el trayecto largo, el dinero o lo que estés viviendo.",
        MichiMood.HAPPY,
    ),
    Page(
        "Pausas de 1 a 3 minutos",
        "Respiraciones y ejercicios cortos que puedes hacer entre clases o en el metro, sin que nadie lo note. Sin presión, a tu ritmo.",
        MichiMood.CALM,
    ),
    Page(
        "Tu diario, solo tuyo",
        "Registra cómo te sientes en segundos. Todo se guarda primero en tu teléfono y funciona sin internet. Si quieres, puedes respaldarlo en tu cuenta.",
        MichiMood.CURIOUS,
    ),
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit, onCrisis: () -> Unit, vm: OnboardingViewModel = koinViewModel()) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var accepted by rememberSaveable { mutableStateOf(false) }
    val isConsent = page == pages.size

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCrisis) { Text("¿Necesitas ayuda ahora?") }
        }
        Spacer(Modifier.height(12.dp))
        if (!isConsent) {
            val p = pages[page]
            Michi(size = 180.dp, mood = p.mood)
            Spacer(Modifier.height(24.dp))
            Text(p.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(12.dp))
            Text(p.body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size + 1) { i ->
                    Box(
                        Modifier.size(if (i == page) 22.dp else 8.dp, 8.dp).clip(CircleShape)
                            .background(if (i == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
            Button(onClick = { page++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Siguiente") }
            if (page > 0) TextButton(onClick = { page-- }) { Text("Atrás") }
        } else {
            Michi(size = 120.dp, mood = MichiMood.CARING)
            Spacer(Modifier.height(16.dp))
            Text("Antes de empezar", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(16.dp))
            InfoCard(container = MaterialTheme.colorScheme.tertiaryContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EstoyContigo no sustituye la atención profesional", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Es una herramienta de acompañamiento y autocuidado. No diagnostica ni da tratamiento psicológico o médico. " +
                            "Si estás en peligro o piensas en hacerte daño, llama al 911 o a la Línea de la Vida 800 911 2000 (24 h, gratuita).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu privacidad", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "• Tus registros se guardan en tu teléfono; la nube es opcional y solo tú puedes leer tus datos.\n" +
                            "• La ubicación se usa solo en el momento para ordenar el directorio; no se guarda.\n" +
                            "• No vendemos ni compartimos tu información, ni usamos publicidad.\n" +
                            "• Puedes borrar todo cuando quieras desde Ajustes.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .toggleable(value = accepted, role = Role.Checkbox, onValueChange = { accepted = it }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = accepted, onCheckedChange = null)
                Spacer(Modifier.size(8.dp))
                Text("Entiendo que es una herramienta de apoyo y acepto el aviso de privacidad.", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.complete(onFinish) }, enabled = accepted,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Comenzar") }
            TextButton(onClick = { page-- }) { Text("Atrás") }
        }
    }
}
