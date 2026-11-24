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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.ipn.upiicsa.estoycontigo.domain.model.ThemeMode
import mx.ipn.upiicsa.estoycontigo.platform.AppPermission
import mx.ipn.upiicsa.estoycontigo.platform.PermissionStatus
import mx.ipn.upiicsa.estoycontigo.platform.rememberPermissionRequester
import mx.ipn.upiicsa.estoycontigo.ui.components.PermissionRationaleDialog
import mx.ipn.upiicsa.estoycontigo.ui.components.SectionTitle
import mx.ipn.upiicsa.estoycontigo.ui.components.SelectableChipRow
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.util.Dates
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

private val reminderTimes = listOf(8 to 0, 13 to 0, 18 to 0, 20 to 0, 22 to 0)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacy: () -> Unit,
    onAccount: () -> Unit,
    onDataDeleted: () -> Unit,
    vm: SettingsViewModel = koinViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val settings = s.settings
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showNotifRationale by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var trustedName by rememberSaveable(settings.trustedContactName) { mutableStateOf(settings.trustedContactName) }
    var trustedPhone by rememberSaveable(settings.trustedContactPhone) { mutableStateOf(settings.trustedContactPhone) }
    var phoneError by rememberSaveable { mutableStateOf(false) }

    val notifications = rememberPermissionRequester(AppPermission.NOTIFICATIONS) { granted ->
        if (granted) pendingAction?.invoke()
        pendingAction = null
    }
    fun withNotifications(action: () -> Unit) {
        if (notifications.status == PermissionStatus.GRANTED) action()
        else { pendingAction = action; showNotifRationale = true }
    }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    AppScreen(title = "Ajustes", onBack = onBack, snackbar = snackbar) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle("Recordatorios")
            ToggleRow("Recordatorio diario “¿Cómo te sientes hoy?”", settings.reminderEnabled) { on ->
                if (on) withNotifications { vm.setReminder(true) } else vm.setReminder(false)
            }
            if (settings.reminderEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminderTimes.forEach { (h, m) ->
                        SelectableChipRow(Dates.hhmm(h, m), settings.reminderHour == h && settings.reminderMinute == m,
                            { vm.setReminder(true, h, m) })
                    }
                }
            }
            ToggleRow("Modo época de exámenes (una pausa sugerida al día)", settings.examMode) { on ->
                if (on) withNotifications { vm.setExamMode(true) } else vm.setExamMode(false)
            }
            Text("Las notificaciones no muestran tus emociones en la pantalla de bloqueo.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Interacción")
            ToggleRow(
                if (s.accelerometerAvailable) "Agitar el teléfono para registrar mi ánimo" else "Agitar para registrar (sin acelerómetro)",
                settings.shakeEnabled && s.accelerometerAvailable, enabled = s.accelerometerAvailable,
            ) { vm.setShake(it) }
            ToggleRow("Vibración suave al cambiar de fase", settings.hapticsEnabled) { vm.setHaptics(it) }
            Text("Tema", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { m -> SelectableChipRow(m.label, settings.themeMode == m, { vm.setTheme(m) }) }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Contacto de confianza")
            Text("Aparecerá en la pantalla SOS para llamarle con un toque. Se guarda solo en tu teléfono.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(trustedName, { trustedName = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                trustedPhone, { trustedPhone = it; phoneError = false }, label = { Text("Teléfono") }, singleLine = true,
                isError = phoneError, supportingText = if (phoneError) { { Text("Escribe un teléfono válido (10 dígitos)") } } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = { phoneError = !vm.saveTrustedContact(trustedName, trustedPhone) }) { Text("Guardar contacto") }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Cuenta y datos")
            Text(s.email?.let { "Sesión: $it" } ?: "Sin cuenta: tus datos están solo en este teléfono.", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (s.email == null) OutlinedButton(onClick = onAccount, enabled = s.cloudAvailable) { Text("Crear cuenta / iniciar sesión") }
                else OutlinedButton(onClick = { vm.signOut() }) { Text("Cerrar sesión") }
            }
            TextButton(onClick = onPrivacy) { Text("Aviso de privacidad") }
            OutlinedButton(
                onClick = { showDelete = true },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.extra.crisis),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Borrar todos mis datos") }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showNotifRationale) {
        PermissionRationaleDialog(
            title = "Permitir notificaciones",
            text = "Para enviarte un recordatorio amable al día. Puedes desactivarlo cuando quieras.",
            permanentlyDenied = notifications.status == PermissionStatus.PERMANENTLY_DENIED,
            onConfirm = { showNotifRationale = false; notifications.request() },
            onOpenSettings = { showNotifRationale = false; vm.actions.openAppSettings() },
            onDismiss = { showNotifRationale = false; pendingAction = null },
        )
    }
    if (showDelete) {
        var includeCloud by rememberSaveable { mutableStateOf(s.email != null) }
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("¿Borrar todos tus datos?") },
            text = {
                Column {
                    Text("Se eliminarán tus registros, fotos y ajustes de este teléfono. Esta acción no se puede deshacer.")
                    if (s.email != null) {
                        Row(
                            Modifier.padding(top = 8.dp).toggleable(includeCloud, role = Role.Checkbox) { includeCloud = it },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(includeCloud, null)
                            Text("También borrar mi cuenta y respaldo en la nube")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDelete = false; vm.deleteAllData(includeCloud, onDataDeleted) }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
