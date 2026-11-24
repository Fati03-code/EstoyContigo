package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.Intervention
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodDraft
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.domain.usecase.InterventionCatalog
import mx.ipn.upiicsa.estoycontigo.domain.usecase.MoodValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskDetector
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskLevel
import mx.ipn.upiicsa.estoycontigo.domain.usecase.ValidationResult
import mx.ipn.upiicsa.estoycontigo.platform.PhotoStorage

data class MoodUiState(
    val emotion: Emotion? = null,
    val intensity: Int = 3,
    val note: String = "",
    val tags: Set<MoodTag> = emptySet(),
    val photoPath: String? = null,
    val errors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saved: SavedOutcome? = null,
)

data class SavedOutcome(val risk: RiskLevel, val recommended: Intervention, val emotion: Emotion)

/**
 * El borrador vive en SavedStateHandle: sobrevive a rotación y a que el sistema mate el proceso
 * (por ejemplo al abrir la cámara en teléfonos con poca memoria).
 */
class MoodViewModel(
    private val handle: SavedStateHandle,
    private val repository: MoodRepository,
    private val photos: PhotoStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(
        MoodUiState(
            emotion = handle.get<String>(K_EMOTION)?.let(Emotion::fromKey),
            intensity = handle.get<Int>(K_INTENSITY) ?: 3,
            note = handle.get<String>(K_NOTE) ?: "",
            tags = MoodTag.parse(handle.get<String>(K_TAGS) ?: ""),
            photoPath = handle.get<String>(K_PHOTO),
        ),
    )
    val state: StateFlow<MoodUiState> = _state.asStateFlow()

    fun selectEmotion(e: Emotion) {
        handle[K_EMOTION] = e.name
        _state.update { it.copy(emotion = e, errors = it.errors - MoodValidator.FIELD_EMOTION) }
    }

    fun setIntensity(v: Int) {
        handle[K_INTENSITY] = v
        _state.update { it.copy(intensity = v) }
    }

    fun setNote(v: String) {
        val trimmed = v.take(MoodValidator.NOTE_MAX_LENGTH)
        handle[K_NOTE] = trimmed
        _state.update { it.copy(note = trimmed, errors = it.errors - MoodValidator.FIELD_NOTE) }
    }

    fun toggleTag(tag: MoodTag) {
        val tags = _state.value.tags.let { if (tag in it) it - tag else it + tag }
        handle[K_TAGS] = MoodTag.toCsv(tags)
        _state.update { it.copy(tags = tags) }
    }

    fun setPhoto(path: String?) {
        if (path == null) return
        val previous = _state.value.photoPath
        handle[K_PHOTO] = path
        _state.update { it.copy(photoPath = path) }
        previous?.let { viewModelScope.launch { photos.delete(it) } }
    }

    fun removePhoto() {
        val previous = _state.value.photoPath ?: return
        handle[K_PHOTO] = null
        _state.update { it.copy(photoPath = null) }
        viewModelScope.launch { photos.delete(previous) }
    }

    fun save() {
        val s = _state.value
        if (s.isSaving) return
        val draft = MoodDraft(s.emotion, s.intensity, s.note, s.tags, s.photoPath)
        val validation = MoodValidator.validate(draft)
        if (validation is ValidationResult.Invalid) {
            _state.update { it.copy(errors = validation.errors) }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val recent = repository.recent(5)
            repository.save(draft).onSuccess { entry ->
                val risk = RiskDetector.evaluate(entry.note, listOf(entry) + recent)
                listOf(K_EMOTION, K_INTENSITY, K_NOTE, K_TAGS, K_PHOTO).forEach { handle.remove<Any>(it) }
                _state.update {
                    MoodUiState(saved = SavedOutcome(risk, InterventionCatalog.recommendedFor(entry.emotion), entry.emotion))
                }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, errors = mapOf("general" to (e.message ?: "No se pudo guardar"))) }
            }
        }
    }

    fun reset() = _state.update { MoodUiState() }

    private companion object {
        const val K_EMOTION = "emotion"
        const val K_INTENSITY = "intensity"
        const val K_NOTE = "note"
        const val K_TAGS = "tags"
        const val K_PHOTO = "photo"
    }
}
