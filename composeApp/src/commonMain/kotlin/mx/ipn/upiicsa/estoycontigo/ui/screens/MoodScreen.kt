package mx.ipn.upiicsa.estoycontigo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.Intensity
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.domain.usecase.MoodValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskLevel
import mx.ipn.upiicsa.estoycontigo.platform.AppPermission
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.PermissionStatus
import mx.ipn.upiicsa.estoycontigo.platform.photoModel
import mx.ipn.upiicsa.estoycontigo.platform.rememberCameraLauncher
import mx.ipn.upiicsa.estoycontigo.platform.rememberPermissionRequester
import mx.ipn.upiicsa.estoycontigo.ui.components.EmotionCard
import mx.ipn.upiicsa.estoycontigo.ui.components.InfoCard
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.components.PermissionRationaleDialog
import mx.ipn.upiicsa.estoycontigo.ui.components.SectionTitle
import mx.ipn.upiicsa.estoycontigo.ui.components.SelectableChipRow
import mx.ipn.upiicsa.estoycontigo.ui.components.toMichiMood
import mx.ipn.upiicsa.estoycontigo.ui.theme.AppTheme
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.MoodViewModel
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.SavedOutcome
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodScreen(
    onClose: () -> Unit,
    onOpenIntervention: (String) -> Unit,
    onCrisis: () -> Unit,
    onDirectory: () -> Unit,
    vm: MoodViewModel = koinViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val actions = koinInject<ExternalActions>()
    var showCameraRationale by rememberSaveable { mutableStateOf(false) }
    val launchCamera = rememberCameraLauncher { path -> vm.setPhoto(path) }
    val cameraPermission = rememberPermissionRequester(AppPermission.CAMERA) { granted -> if (granted) launchCamera() }

    val saved = s.saved
    if (saved != null) {
        SavedView(saved, onClose, onOpenIntervention, onCrisis, onDirectory)
        return
    }

    AppScreen(title = "¿Cómo te sientes?", onBack = onClose, onSos = onCrisis) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Michi(size = 72.dp, mood = s.emotion.toMichiMood())
                Spacer(Modifier.width(8.dp))
                Text("Elige la emoción que más se acerque. Todas son válidas.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Emotion.entries.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { e ->
                        EmotionCard(e, selected = s.emotion == e, onClick = { vm.selectEmotion(e) }, modifier = Modifier.weight(1f))
                    }
                }
            }
            s.errors[MoodValidator.FIELD_EMOTION]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }

            SectionTitle("Intensidad: ${Intensity.label(s.intensity)}")
            Slider(
                value = s.intensity.toFloat(), onValueChange = { vm.setIntensity(it.toInt()) },
                valueRange = Intensity.MIN.toFloat()..Intensity.MAX.toFloat(), steps = 3,
                modifier = Modifier.semantics { stateDescription = "${s.intensity} de 5, ${Intensity.label(s.intensity)}" },
            )

            SectionTitle("¿Con qué tiene que ver? (opcional)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MoodTag.entries.forEach { tag ->
                    SelectableChipRow(tag.label, selected = tag in s.tags, onClick = { vm.toggleTag(tag) })
                }
            }

            SectionTitle("Nota para ti (opcional)")
            OutlinedTextField(
                value = s.note, onValueChange = vm::setNote,
                placeholder = { Text("¿Qué pasó hoy? Escribe lo que necesites soltar.") },
                minLines = 3, modifier = Modifier.fillMaxWidth(),
                isError = s.errors.containsKey(MoodValidator.FIELD_NOTE),
                supportingText = { Text("${s.note.length}/${MoodValidator.NOTE_MAX_LENGTH}") },
            )

            SectionTitle("Foto del día (opcional)")
            if (s.photoPath != null) {
                Box {
                    AsyncImage(
                        model = photoModel(s.photoPath!!), contentDescription = "Foto adjunta al registro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large),
                    )
                    FilledTonalIconButton(onClick = vm::removePhoto, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Icon(Icons.Outlined.Close, "Quitar foto")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        if (cameraPermission.status == PermissionStatus.GRANTED) launchCamera() else showCameraRationale = true
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar foto: un cielo, tu café, algo que te dio paz")
                }
            }
            Text("La foto se guarda privada en tu teléfono y sin datos de ubicación.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            s.errors["general"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = vm::save, enabled = !s.isSaving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text("Guardar registro") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCameraRationale) {
        PermissionRationaleDialog(
            title = "Usar la cámara",
            text = "EstoyContigo usa la cámara solo cuando tú decides tomar una foto para tu diario. La foto no se comparte con nadie.",
            permanentlyDenied = cameraPermission.status == PermissionStatus.PERMANENTLY_DENIED,
            onConfirm = { showCameraRationale = false; cameraPermission.request() },
            onOpenSettings = { showCameraRationale = false; actions.openAppSettings() },
            onDismiss = { showCameraRationale = false },
        )
    }
}

@Composable
private fun SavedView(
    saved: SavedOutcome,
    onClose: () -> Unit,
    onOpenIntervention: (String) -> Unit,
    onCrisis: () -> Unit,
    onDirectory: () -> Unit,
) {
    AppScreen(title = "Registro guardado", onBack = onClose, onSos = onCrisis) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (saved.risk) {
                RiskLevel.POSSIBLE_CRISIS -> {
                    Michi(size = 120.dp, mood = MichiMood.CARING)
                    InfoCard(container = AppTheme.extra.crisis.copy(alpha = 0.12f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Gracias por confiarlo. No estás sola ni solo.", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Lo que escribiste suena muy doloroso. Hablar ahora con una persona puede ayudarte. " +
                                    "Las líneas son gratuitas, confidenciales y atienden las 24 horas.",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                    Button(onClick = onCrisis, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text("Hablar con alguien ahora")
                    }
                    OutlinedButton(onClick = { onOpenIntervention("suspiro") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Primero respirar 1 minuto con Michi")
                    }
                }
                RiskLevel.SUSTAINED_DISTRESS -> {
                    Michi(size = 120.dp, mood = MichiMood.CARING)
                    Text("Han sido días difíciles", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    Text(
                        "Notamos varios registros pesados seguidos. Pedir apoyo es una muestra de fuerza: " +
                            "Orientación Juvenil de UPIICSA ofrece atención psicológica gratuita para estudiantes.",
                        style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    )
                    Button(onClick = onDirectory, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Ver opciones de apoyo") }
                    OutlinedButton(onClick = { onOpenIntervention(saved.recommended.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("${saved.recommended.title} · ${saved.recommended.durationLabel}")
                    }
                }
                RiskLevel.NONE -> {
                    Michi(size = 140.dp, mood = if (saved.emotion.isDifficult) MichiMood.CARING else MichiMood.HAPPY)
                    Text(if (saved.emotion.isDifficult) "Gracias por darte este momento" else "¡Qué bueno!",
                        style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (saved.emotion.isDifficult) "Nombrar lo que sientes ya ayuda a regularlo. ¿Te late una pausa corta?"
                        else "Guardar los momentos buenos también ayuda en los días difíciles.",
                        style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    )
                    Button(onClick = { onOpenIntervention(saved.recommended.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("${saved.recommended.title} · ${saved.recommended.durationLabel}")
                    }
                }
            }
            TextButton(onClick = onClose) { Text("Volver al inicio") }
        }
    }
}
