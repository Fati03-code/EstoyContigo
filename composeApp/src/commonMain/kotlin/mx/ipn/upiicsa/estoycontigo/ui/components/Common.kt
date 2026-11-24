package mx.ipn.upiicsa.estoycontigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.SyncState
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.theme.colors

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        action?.invoke()
    }
}

/** Indicador Offline-First: naranja = pendiente en el teléfono, verde = respaldado en la nube. */
@Composable
fun SyncBadge(state: SyncState, modifier: Modifier = Modifier, compact: Boolean = false) {
    val synced = state == SyncState.SYNCED
    val color = if (synced) AppTheme.extra.synced else AppTheme.extra.pending
    val label = if (synced) "Respaldado" else "Pendiente"
    Row(
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (synced) "Sincronizado en la nube" else "Pendiente de sincronizar, guardado en el teléfono"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (synced) Icons.Outlined.CloudDone else Icons.Outlined.CloudUpload, null,
            tint = color, modifier = Modifier.size(16.dp),
        )
        if (!compact) {
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmotionDot(emotion: Emotion?, modifier: Modifier = Modifier, sizeDp: Int = 14) {
    val dark = AppTheme.extra.isDark
    Box(
        modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(emotion?.colors(dark)?.base ?: Color.Transparent)
            .then(if (emotion == null) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape) else Modifier),
    )
}

@Composable
fun EmotionCard(emotion: Emotion, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = emotion.colors(AppTheme.extra.isDark)
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 96.dp)
            .semantics {
                this.selected = selected
                contentDescription = "${emotion.label}. ${emotion.description}"
            },
        shape = MaterialTheme.shapes.medium,
        color = if (selected) c.container else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.base else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            EmotionFace(emotion, size = 40)
            Spacer(Modifier.padding(top = 6.dp))
            Text(emotion.label, style = MaterialTheme.typography.labelLarge, color = if (selected) c.onContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = container)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colors, shape = MaterialTheme.shapes.large) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colors, shape = MaterialTheme.shapes.large) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Composable
fun IconBubble(icon: ImageVector, tint: Color, background: Color, modifier: Modifier = Modifier, sizeDp: Int = 44) {
    Box(modifier.size(sizeDp.dp).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((sizeDp * 0.5f).dp))
    }
}

/** Diálogo que explica para qué se usa un permiso antes de pedirlo (o lleva a Ajustes si se negó). */
@Composable
fun PermissionRationaleDialog(
    title: String,
    text: String,
    permanentlyDenied: Boolean,
    onConfirm: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(
                if (permanentlyDenied) "$text\n\nEl permiso está desactivado. Puedes activarlo en Ajustes del sistema; la app sigue funcionando sin él." else text,
            )
        },
        confirmButton = {
            if (permanentlyDenied) TextButton(onClick = onOpenSettings) { Text("Abrir Ajustes") }
            else TextButton(onClick = onConfirm) { Text("Continuar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ahora no") } },
    )
}

@Composable
fun SelectableChipRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp).semantics { this.selected = selected; role = Role.Checkbox },
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun clickableRow(onClick: () -> Unit): Modifier = Modifier.clickable(onClick = onClick)
