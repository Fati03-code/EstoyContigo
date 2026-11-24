package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.DirectionsSubway
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.domain.model.InterventionKind
import mx.ipn.upiicsa.estoycontigo.domain.usecase.InterventionCatalog
import mx.ipn.upiicsa.estoycontigo.ui.components.IconBubble
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood

@Composable
fun InterventionsScreen(onMenu: () -> Unit, onSos: () -> Unit, onOpen: (String) -> Unit) {
    AppScreen(title = "Micro-pausas", onMenu = onMenu, onSos = onSos) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Michi(size = 80.dp, mood = MichiMood.CALM)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ejercicios de 1 a 3 minutos para hacer entre clases, en la fila o en el transporte. " +
                            "Los marcados como discretos se pueden hacer con los ojos abiertos.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(InterventionCatalog.all, key = { it.id }) { item ->
                val icon = when {
                    item.id == "grounding" -> Icons.Outlined.Visibility
                    item.id == "compasion" -> Icons.Outlined.Favorite
                    item.kind is InterventionKind.Guided -> Icons.Outlined.Spa
                    else -> Icons.Outlined.Air
                }
                InfoCard(onClick = { onOpen(item.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(icon, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.padding(top = 4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.durationLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                if (item.discreet) {
                                    Spacer(Modifier.width(10.dp))
                                    androidx.compose.material3.Icon(Icons.Outlined.DirectionsSubway, null,
                                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 4.dp))
                                    Text("Discreta", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Text(item.whenToUse, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
