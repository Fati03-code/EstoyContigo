package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.DirectoryRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.domain.model.UserSettings
import mx.ipn.upiicsa.estoycontigo.platform.ConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.MotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler

data class AppState(
    val settings: UserSettings? = null,
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
)

class AppViewModel(
    prefs: PreferencesStore,
    private val directory: DirectoryRepository,
    moods: MoodRepository,
    connectivity: ConnectivityMonitor,
    private val syncScheduler: SyncScheduler,
    motion: MotionSensor,
) : ViewModel() {

    val state: StateFlow<AppState> = combine(prefs.settings, connectivity.isOnline, moods.pendingCount) { s, online, pending ->
        AppState(s, online, pending)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppState())

    /** Sacudidas del teléfono (solo si la persona lo activó en Ajustes). */
    val shakes = prefs.settings.map { it.shakeEnabled && motion.isAvailable }
        .flatMapLatest { enabled -> if (enabled) motion.shakes() else emptyFlow() }

    init {
        viewModelScope.launch { directory.ensureSeeded() }
        // Al reconectar se sube la cola de pendientes (Queue & Sync).
        viewModelScope.launch {
            connectivity.isOnline.filter { it }.collect { syncScheduler.requestSync() }
        }
    }
}
