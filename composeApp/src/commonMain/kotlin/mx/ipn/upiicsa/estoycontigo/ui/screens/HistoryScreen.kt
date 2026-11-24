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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.Intensity
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.platform.photoModel
import mx.ipn.upiicsa.estoycontigo.ui.components.EmotionFace
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.components.SectionTitle
import mx.ipn.upiicsa.estoycontigo.ui.components.SyncBadge
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.theme.colors
import mx.ipn.upiicsa.estoycontigo.ui.util.Dates
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.HistoryUiState
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onMenu: () -> Unit,
    onSos: () -> Unit,
    onRegister: () -> Unit,
    onAccount: () -> Unit,
    vm: HistoryViewModel = koinViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var toDelete by rememberSaveable { mutableStateOf<String?>(null) }

    AppScreen(
        title = "Mi diario", onMenu = onMenu, onSos = onSos,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onRegister, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Registrar") })
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SyncCard(s, onSync = vm::syncNow, onAccount = onAccount) }
            if (s.isLoaded && s.groups.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Michi(size = 140.dp, mood = MichiMood.CURIOUS)
                        Spacer(Modifier.height(12.dp))
                        Text("Tu diario está vacío", style = MaterialTheme.typography.titleLarge)
                        Text("Tu primer registro toma menos de un minuto.", textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (s.emotionCounts30d.isNotEmpty()) {
                item { MonthSummary(s) }
            }
            s.groups.forEach { (date, entries) ->
                item(key = "h-$date") { SectionTitle(Dates.dayLabel(date)) }
                items(entries, key = { it.id }) { entry -> EntryCard(entry, onDelete = { toDelete = entry.id }) }
            }
        }
    }

    toDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar registro?") },
            text = { Text("Se borrará de este teléfono y, si estaba respaldado, también de la nube.") },
            confirmButton = { TextButton(onClick = { vm.delete(id); toDelete = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun SyncCard(s: HistoryUiState, onSync: () -> Unit, onAccount: () -> Unit) {
    val pending = s.pendingCount > 0
    InfoCard(container = (if (pending) AppTheme.extra.pending else AppTheme.extra.synced).copy(alpha = 0.10f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        !s.signedIn -> "Guardado solo en este teléfono"
                        pending && !s.isOnline -> "${s.pendingCount} pendiente(s): se subirán al reconectar"
                        pending -> "${s.pendingCount} pendiente(s) de respaldar"
                        else -> "Todo respaldado"
                    },
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    when {
                        !s.signedIn -> "Crea una cuenta para respaldar tu diario (opcional)."
                        s.lastSyncAt > 0 -> "Último respaldo: ${Dates.dayLabel(Dates.localDate(s.lastSyncAt))} ${Dates.time(s.lastSyncAt)}"
                        else -> "Naranja = en tu teléfono · Verde = respaldado"
                    },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!s.signedIn) TextButton(onClick = onAccount) { Text("Respaldar") }
            else if (pending && s.isOnline) IconButton(onClick = onSync) { Icon(Icons.Outlined.Sync, "Sincronizar ahora") }
        }
    }
}

@Composable
private fun MonthSummary(s: HistoryUiState) {
    val total = s.emotionCounts30d.values.sum().coerceAtLeast(1)
    val dark = AppTheme.extra.isDark
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Últimos 30 días", style = MaterialTheme.typography.titleMedium)
            Emotion.entries.filter { (s.emotionCounts30d[it] ?: 0) > 0 }.forEach { e ->
                val count = s.emotionCounts30d[e] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.label, Modifier.width(92.dp), style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { count.toFloat() / total },
                        color = e.colors(dark).base, trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape),
                    )
                    Text("  $count", style = MaterialTheme.typography.labelLarge)
                }
            }
            if (s.topTags30d.isNotEmpty()) {
                Text(
                    "Lo que más se repite: " + s.topTags30d.joinToString { it.first.label.lowercase() },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EntryCard(entry: MoodEntry, onDelete: () -> Unit) {
    val c = entry.emotion.colors(AppTheme.extra.isDark)
    InfoCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmotionFace(entry.emotion, size = 40)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.emotion.label, style = MaterialTheme.typography.titleMedium)
                    Text("${Dates.time(entry.createdAt)} · ${Intensity.label(entry.intensity)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SyncBadge(entry.syncState, compact = false)
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Eliminar registro de ${entry.emotion.label}") }
            }
            if (entry.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.take(3).forEach { t ->
                        Text(t.label, style = MaterialTheme.typography.labelMedium, color = c.onContainer,
                            modifier = Modifier.clip(CircleShape).background(c.container).padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    if (entry.tags.size > 3) Text("+${entry.tags.size - 3}", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(entry.note, style = MaterialTheme.typography.bodyMedium)
            }
            entry.photoPath?.let {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = photoModel(it), contentDescription = "Foto del registro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.medium),
                )
            }
        }
    }
}
