package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory
import mx.ipn.upiicsa.estoycontigo.domain.model.SupportService
import mx.ipn.upiicsa.estoycontigo.domain.usecase.GeoDistance
import mx.ipn.upiicsa.estoycontigo.platform.AppPermission
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.PermissionStatus
import mx.ipn.upiicsa.estoycontigo.platform.rememberPermissionRequester
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.PermissionRationaleDialog
import mx.ipn.upiicsa.estoycontigo.ui.components.SelectableChipRow
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.DirectoryViewModel
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.LocationState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DirectoryScreen(onMenu: () -> Unit, onSos: () -> Unit, vm: DirectoryViewModel = koinViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var showRationale by rememberSaveable { mutableStateOf(false) }
    val location = rememberPermissionRequester(AppPermission.LOCATION) { granted -> if (granted) vm.locate() else vm.locationDenied() }

    AppScreen(title = "Directorio de apoyo", onMenu = onMenu, onSos = onSos) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Disponible sin internet. Llamar abre tu marcador; tú decides si marcar.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChipRow("Todos", s.filter == null, { vm.setFilter(null) })
                    ServiceCategory.entries.forEach { c -> SelectableChipRow(c.label, s.filter == c, { vm.setFilter(c) }) }
                }
            }
            item {
                InfoCard(container = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MyLocation, null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            when (s.locationState) {
                                LocationState.FOUND -> "Ordenado por cercanía a ti"
                                LocationState.LOADING -> "Buscando tu ubicación…"
                                LocationState.UNAVAILABLE -> "Sin ubicación: se muestra el orden recomendado"
                                LocationState.IDLE -> "Ordena por cercanía usando tu ubicación (opcional)"
                            },
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
                        )
                        if (s.locationState == LocationState.LOADING) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else if (s.locationState != LocationState.FOUND) {
                            FilledTonalButton(onClick = {
                                if (location.status == PermissionStatus.GRANTED) vm.locate() else showRationale = true
                            }) { Text("Usar ubicación") }
                        }
                    }
                }
            }
            items(s.services, key = { it.id }) { ServiceCard(it, vm.actions) }
            item {
                Text("Información verificada en fuentes oficiales (nov 2026). Los horarios pueden cambiar; confirma antes de acudir.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showRationale) {
        PermissionRationaleDialog(
            title = "Usar tu ubicación",
            text = "Solo para ordenar el directorio por cercanía en este momento. No guardamos ni enviamos tu ubicación.",
            permanentlyDenied = location.status == PermissionStatus.PERMANENTLY_DENIED,
            onConfirm = { showRationale = false; location.request() },
            onOpenSettings = { showRationale = false; vm.actions.openAppSettings() },
            onDismiss = { showRationale = false },
        )
    }
}

@Composable
fun ServiceCard(service: SupportService, actions: ExternalActions) {
    val accent = when (service.category) {
        ServiceCategory.CRISIS -> AppTheme.extra.crisis
        ServiceCategory.UPIICSA -> MaterialTheme.colorScheme.primary
        ServiceCategory.CLINIC -> MaterialTheme.colorScheme.secondary
    }
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(service.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                service.distanceKm?.let { Text(GeoDistance.format(it), style = MaterialTheme.typography.labelLarge, color = accent) }
            }
            Text(service.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                (if (service.is24h) "24 horas · " else "") + service.schedule,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            service.address?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                service.phone?.let { phone ->
                    FilledTonalButton(onClick = { actions.dial(phone) }) {
                        Icon(Icons.Outlined.Call, null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (service.phoneExtension != null) "Llamar (ext. ${service.phoneExtension})" else "Llamar")
                    }
                }
                if (service.latitude != null && service.longitude != null) {
                    OutlinedButton(onClick = { actions.openMap(service.latitude, service.longitude, service.name) }) {
                        Icon(Icons.Outlined.Map, null); Spacer(Modifier.width(6.dp)); Text("Mapa")
                    }
                }
                service.email?.let { mail ->
                    AssistChip(onClick = { actions.sendEmail(mail) }, label = { Text("Correo") },
                        leadingIcon = { Icon(Icons.Outlined.Email, null) })
                }
            }
        }
    }
}
