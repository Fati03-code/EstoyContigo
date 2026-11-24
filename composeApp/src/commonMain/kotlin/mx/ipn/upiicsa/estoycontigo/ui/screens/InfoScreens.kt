package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood

/** Resumen del aviso de privacidad; el texto completo está en docs/PRIVACIDAD.md. */
private val privacySections = listOf(
    "Responsable" to "EstoyContigo es un proyecto académico de estudiantes de UPIICSA-IPN para la materia Programación de Dispositivos Móviles. Contacto: el correo indicado en la ficha de la tienda o en el repositorio del proyecto.",
    "Qué datos tratamos" to "• Registros de ánimo: emoción, intensidad, motivos, nota y foto opcional (datos personales sensibles según la LFPDPPP).\n• Correo y contraseña si decides crear una cuenta (la contraseña la gestiona Firebase Authentication y nunca la vemos).\n• Preferencias: horario de recordatorio, tema, contacto de confianza (solo en tu teléfono).\n• Ubicación aproximada: solo en el momento, para ordenar el directorio; no se guarda ni se envía.",
    "Para qué" to "Únicamente para mostrarte tu diario, recomendarte micro-pausas y respaldar tus datos si lo pides. No usamos tus datos para publicidad, perfiles comerciales ni los vendemos o compartimos con terceros.",
    "Dónde se guardan" to "Primero en tu teléfono (base de datos local privada de la app, excluida de respaldos del sistema). Si creas cuenta, en Google Firebase (Firestore y Storage) con cifrado en tránsito (HTTPS) y en reposo; las reglas de seguridad permiten que solo tu cuenta lea o escriba tus registros.",
    "Consentimiento" to "Al aceptar el aviso en el primer uso otorgas tu consentimiento expreso para el tratamiento de tus datos sensibles con las finalidades descritas. Puedes revocarlo en cualquier momento borrando tus datos.",
    "Tus derechos ARCO" to "Puedes acceder a tus datos (Mi diario), rectificarlos o cancelarlos (eliminar un registro o \"Borrar todos mis datos\" en Ajustes, que también elimina la cuenta y el respaldo en la nube) y oponerte a su tratamiento dejando de usar la app.",
    "Menores de edad" to "La app está dirigida a estudiantes universitarios mayores de 18 años.",
    "Cambios" to "Si este aviso cambia, te lo mostraremos en la app antes de seguir usándola.",
)

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    AppScreen(title = "Aviso de privacidad", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Aviso de privacidad integral · Versión 1.0 (noviembre 2026)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            privacySections.forEach { (title, body) ->
                InfoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
                        Text(body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit, onCrisis: () -> Unit) {
    AppScreen(title = "Acerca de", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Michi(size = 120.dp, mood = MichiMood.HAPPY)
            Text("EstoyContigo 1.0", style = MaterialTheme.typography.headlineSmall)
            Text("Acompañamiento emocional para estudiantes de UPIICSA-IPN.", style = MaterialTheme.typography.bodyMedium)
            InfoCard(container = MaterialTheme.colorScheme.tertiaryContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Aviso importante", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "EstoyContigo es una herramienta de autocuidado y acompañamiento. No ofrece diagnóstico, tratamiento ni " +
                            "terapia, y no sustituye la atención de profesionales de la salud mental. La detección de frases de riesgo " +
                            "es orientativa y puede fallar: si estás en riesgo, busca ayuda humana de inmediato.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            InfoCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fundamento de las micro-pausas", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Las técnicas incluidas (suspiro fisiológico, respiración cuadrada, 4-7-8, respiración a ~6 por minuto, " +
                            "grounding 5-4-3-2-1 y autocompasión) son ejercicios de autorregulación de uso general y bajo riesgo. " +
                            "Se adaptaron a 1–3 minutos para horarios fragmentados.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Button(onClick = onCrisis, modifier = Modifier.fillMaxWidth()) { Text("Ver líneas de ayuda") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
