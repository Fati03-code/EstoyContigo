package com.ipn.upiicsa.estoycontigo.equipo5cm50.ui.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    viewModel: MoodViewModel,
    modifier: Modifier = Modifier
) {
    val moods by viewModel.moods.collectAsStateWithLifecycle()

    var selectedLevel by remember { mutableIntStateOf(3) }
    var emotionLabel by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val quickEmotions = listOf("Tranquilo", "Feliz", "Cansado", "Estresado", "Ansioso")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estoy Contigo • Mi Ánimo") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "¿Cómo te sientes hoy?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text("Nivel $level") }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickEmotions.take(3).forEach { label ->
                    AssistChip(
                        onClick = { emotionLabel = label },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = emotionLabel,
                onValueChange = { emotionLabel = it },
                label = { Text("Emoción principal") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota breve (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (emotionLabel.isNotBlank()) {
                        viewModel.addMood(selectedLevel, emotionLabel, note)
                        emotionLabel = ""
                        note = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar registro offline")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "Historial guardado localmente (${moods.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moods, key = { it.id }) { entry ->
                    MoodItemCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun MoodItemCard(entry: MoodEntry) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${entry.emotionLabel} (Nivel ${entry.moodLevel}/5)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (entry.isSynced) "Sincronizado" else "Local",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (entry.note.isNotBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = dateFormat.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}