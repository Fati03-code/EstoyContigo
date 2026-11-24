package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.ui.components.EmotionDot
import mx.ipn.upiicsa.estoycontigo.ui.components.IconBubble
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.SectionTitle
import mx.ipn.upiicsa.estoycontigo.ui.components.SyncBadge
import mx.ipn.upiicsa.estoycontigo.ui.components.toMichiMood
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.theme.colors
import mx.ipn.upiicsa.estoycontigo.ui.util.Dates
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onMenu: () -> Unit,
    onSos: () -> Unit,
    onRegister: () -> Unit,
    onOpenIntervention: (String) -> Unit,
    onInterventions: () -> Unit,
    onHistory: () -> Unit,
    onDirectory: () -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val dark = AppTheme.extra.isDark
    AppScreen(title = "EstoyContigo", onMenu = onMenu, onSos = onSos) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                val today = s.todayEntry
                val container = today?.emotion?.colors(dark)?.container ?: MaterialTheme.colorScheme.primaryContainer
                InfoCard(container = container) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(Dates.greeting(), style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (today == null) "¿Cómo te sientes hoy? Tomarte un minuto para notarlo ya es cuidarte."
                                else "Hoy registraste ${today.emotion.label.lowercase()}. Gracias por darte ese espacio.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(12.dp))
                            if (today == null) {
                                Button(onClick = onRegister) {
                                    Icon(Icons.Outlined.Edit, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Registrar mi ánimo")
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SyncBadge(today.syncState)
                                    TextButton(onClick = onRegister) { Text("Agregar otro") }
                                }
                            }
                        }
                        Michi(size = 104.dp, mood = today?.emotion.toMichiMood())
                    }
                }
            }

            if (s.sustainedDistress) {
                item {
                    InfoCard(container = MaterialTheme.colorScheme.tertiaryContainer) {
                        Column {
                            Text("Michi notó que han sido días pesados", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Varios de tus registros recientes han sido difíciles. No tienes que poder con todo tú sola o solo: " +
                                    "hablar con alguien de confianza o con Orientación Juvenil de UPIICSA puede ayudar.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row {
                                TextButton(onClick = onDirectory) { Text("Ver apoyo") }
                                TextButton(onClick = onSos) { Text("Ayuda inmediata") }
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("Tu pausa recomendada")
                val rec = s.recommended
                InfoCard(onClick = { onOpenIntervention(rec.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(Icons.Outlined.SelfImprovement, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(rec.title, style = MaterialTheme.typography.titleMedium)
                            Text("${rec.durationLabel} · ${if (rec.discreet) "discreta, ideal en el transporte" else rec.subtitle}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Empezar ${rec.title}")
                    }
                }
            }

            item {
                SectionTitle("Tus últimos 7 días", action = { TextButton(onClick = onHistory) { Text("Ver diario") } })
                InfoCard {
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            s.week.forEach { d ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.semantics(mergeDescendants = true) {
                                        contentDescription = "${Dates.dayLabel(d.date)}: ${d.emotion?.label ?: "sin registro"}"
                                    },
                                ) {
                                    EmotionDot(d.emotion, sizeDp = 26)
                                    Spacer(Modifier.height(6.dp))
                                    Text(Dates.shortDay(d.date), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            when {
                                s.totalEntries == 0 -> "Aquí verás tus colores de la semana. No hay respuestas correctas."
                                s.streakDays >= 2 -> "Llevas ${s.streakDays} días registrando. Sin presión: cada registro cuenta."
                                else -> "Registrar seguido te ayuda a reconocer patrones, como semanas de exámenes."
                            },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (s.examMode) {
                item {
                    InfoCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.School, null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Modo exámenes activo: tu valor no se mide en calificaciones. Duerme, come y toma pausas cortas entre temas.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("Atajos")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onInterventions, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.SelfImprovement, null); Spacer(Modifier.width(6.dp)); Text("Pausas")
                    }
                    OutlinedButton(onClick = onDirectory, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.LocationOn, null); Spacer(Modifier.width(6.dp)); Text("Apoyo")
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Vibration, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("Tip: agita el teléfono para registrar tu ánimo rápidamente.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "EstoyContigo es una herramienta de acompañamiento y no sustituye la atención psicológica o médica profesional.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
