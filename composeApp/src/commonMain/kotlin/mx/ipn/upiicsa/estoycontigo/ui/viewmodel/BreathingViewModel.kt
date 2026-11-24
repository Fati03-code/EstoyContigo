package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.domain.model.BreathPhaseType
import mx.ipn.upiicsa.estoycontigo.domain.model.Intervention
import mx.ipn.upiicsa.estoycontigo.domain.model.InterventionKind
import mx.ipn.upiicsa.estoycontigo.domain.usecase.InterventionCatalog
import mx.ipn.upiicsa.estoycontigo.platform.Haptics
import mx.ipn.upiicsa.estoycontigo.platform.MotionSensor

enum class PlayerStatus { READY, RUNNING, PAUSED, DONE }

data class PlayerFrame(
    val label: String,
    /** 0..1 dentro de la fase/paso actual. */
    val phaseProgress: Float,
    val phaseSecondsLeft: Int,
    val phaseType: BreathPhaseType?,
    val stepIndex: Int,
)

data class BreathingUiState(
    val intervention: Intervention,
    val status: PlayerStatus = PlayerStatus.READY,
    val elapsedMs: Long = 0,
) {
    val totalMs: Long get() = intervention.durationSeconds * 1000L
    val progress: Float get() = (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)
    val secondsLeft: Int get() = ((totalMs - elapsedMs + 999) / 1000).toInt().coerceAtLeast(0)
    val frame: PlayerFrame get() = frameAt(intervention, elapsedMs)
}

fun frameAt(intervention: Intervention, elapsedMs: Long): PlayerFrame = when (val kind = intervention.kind) {
    is InterventionKind.Breathing -> {
        val cycleMs = kind.phases.sumOf { it.seconds } * 1000L
        var t = elapsedMs % cycleMs
        var result: PlayerFrame? = null
        for (phase in kind.phases) {
            val len = phase.seconds * 1000L
            if (t < len) {
                result = PlayerFrame(
                    label = phase.type.label,
                    phaseProgress = t.toFloat() / len,
                    phaseSecondsLeft = ((len - t + 999) / 1000).toInt(),
                    phaseType = phase.type,
                    stepIndex = kind.phases.indexOf(phase),
                )
                break
            }
            t -= len
        }
        result ?: PlayerFrame(kind.phases.first().type.label, 0f, kind.phases.first().seconds, kind.phases.first().type, 0)
    }
    is InterventionKind.Guided -> {
        val stepMs = kind.secondsPerStep * 1000L
        val index = (elapsedMs / stepMs).toInt().coerceAtMost(kind.steps.lastIndex)
        val t = elapsedMs - index * stepMs
        PlayerFrame(
            label = kind.steps[index],
            phaseProgress = (t.toFloat() / stepMs).coerceIn(0f, 1f),
            phaseSecondsLeft = ((stepMs - t + 999) / 1000).toInt().coerceAtLeast(0),
            phaseType = null,
            stepIndex = index,
        )
    }
}

class BreathingViewModel(
    private val handle: SavedStateHandle,
    motion: MotionSensor,
    prefs: PreferencesStore,
    private val haptics: Haptics,
) : ViewModel() {

    private val interventionId: String = handle.get<String>("id") ?: InterventionCatalog.box.id
    private val _state = MutableStateFlow(
        BreathingUiState(
            intervention = InterventionCatalog.byId(interventionId) ?: InterventionCatalog.box,
            elapsedMs = handle.get<Long>(K_ELAPSED) ?: 0L,
            status = handle.get<String>(K_STATUS)?.let { s -> PlayerStatus.entries.firstOrNull { it.name == s } }
                ?.let { if (it == PlayerStatus.RUNNING) PlayerStatus.PAUSED else it } ?: PlayerStatus.READY,
        ),
    )
    val state: StateFlow<BreathingUiState> = _state.asStateFlow()

    private val hapticsEnabled = prefs.settings.map { it.hapticsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** Detecta si la persona va en movimiento (p. ej. caminando o en el metro) para sugerir la versión con ojos abiertos. */
    val isMoving: StateFlow<Boolean> = _state.map { it.status == PlayerStatus.RUNNING }
        .flatMapLatest { running -> if (running) motion.movement().map { it > MOVING_THRESHOLD } else emptyFlow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2_000), false)

    private var ticker: Job? = null

    fun start() {
        if (_state.value.status == PlayerStatus.RUNNING) return
        if (_state.value.status == PlayerStatus.DONE) _state.update { it.copy(elapsedMs = 0) }
        _state.update { it.copy(status = PlayerStatus.RUNNING) }
        persist()
        ticker?.cancel()
        ticker = viewModelScope.launch {
            var lastStep = _state.value.frame.stepIndex
            while (_state.value.status == PlayerStatus.RUNNING) {
                delay(TICK_MS)
                val next = _state.value.elapsedMs + TICK_MS
                if (next >= _state.value.totalMs) {
                    _state.update { it.copy(elapsedMs = it.totalMs, status = PlayerStatus.DONE) }
                    if (hapticsEnabled.value) haptics.tick()
                } else {
                    _state.update { it.copy(elapsedMs = next) }
                    val step = _state.value.frame.stepIndex
                    if (step != lastStep && hapticsEnabled.value) haptics.tick()
                    lastStep = step
                }
                persist()
            }
        }
    }

    fun pause() {
        if (_state.value.status != PlayerStatus.RUNNING) return
        _state.update { it.copy(status = PlayerStatus.PAUSED) }
        ticker?.cancel()
        persist()
    }

    fun restart() {
        ticker?.cancel()
        _state.update { it.copy(elapsedMs = 0, status = PlayerStatus.READY) }
        persist()
    }

    private fun persist() {
        handle[K_ELAPSED] = _state.value.elapsedMs
        handle[K_STATUS] = _state.value.status.name
    }

    private companion object {
        const val TICK_MS = 100L
        const val MOVING_THRESHOLD = 1.2f
        const val K_ELAPSED = "elapsed"
        const val K_STATUS = "status"
    }
}
