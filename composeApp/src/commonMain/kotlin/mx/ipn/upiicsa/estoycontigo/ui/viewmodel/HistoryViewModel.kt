package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.AuthRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodTag
import mx.ipn.upiicsa.estoycontigo.platform.ConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.ui.util.Dates

data class HistoryUiState(
    val groups: List<Pair<LocalDate, List<MoodEntry>>> = emptyList(),
    val pendingCount: Int = 0,
    val isOnline: Boolean = true,
    val signedIn: Boolean = false,
    val lastSyncAt: Long = 0,
    val emotionCounts30d: Map<Emotion, Int> = emptyMap(),
    val topTags30d: List<Pair<MoodTag, Int>> = emptyList(),
    val isLoaded: Boolean = false,
)

class HistoryViewModel(
    private val moods: MoodRepository,
    prefs: PreferencesStore,
    auth: AuthRepository,
    connectivity: ConnectivityMonitor,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    val state: StateFlow<HistoryUiState> = combine(
        moods.entries, moods.pendingCount, connectivity.isOnline, auth.session, prefs.settings,
    ) { entries, pending, online, session, settings ->
        val since = Dates.lastDays(30).first()
        val recent = entries.filter { Dates.localDate(it.createdAt) >= since }
        HistoryUiState(
            groups = entries.groupBy { Dates.localDate(it.createdAt) }.toList(),
            pendingCount = pending,
            isOnline = online,
            signedIn = session != null,
            lastSyncAt = settings.lastSyncAt,
            emotionCounts30d = recent.groupingBy { it.emotion }.eachCount(),
            topTags30d = recent.flatMap { it.tags }.groupingBy { it }.eachCount().toList()
                .sortedByDescending { it.second }.take(3),
            isLoaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun delete(id: String) = viewModelScope.launch { moods.delete(id) }

    fun syncNow() = syncScheduler.requestSync()
}
