package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.Intervention
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodEntry
import mx.ipn.upiicsa.estoycontigo.domain.usecase.InterventionCatalog
import mx.ipn.upiicsa.estoycontigo.domain.usecase.RiskDetector
import mx.ipn.upiicsa.estoycontigo.ui.util.Dates

data class DaySummary(val date: LocalDate, val emotion: Emotion?)

data class HomeUiState(
    val todayEntry: MoodEntry? = null,
    val week: List<DaySummary> = emptyList(),
    val streakDays: Int = 0,
    val recommended: Intervention = InterventionCatalog.box,
    val sustainedDistress: Boolean = false,
    val examMode: Boolean = false,
    val totalEntries: Int = 0,
)

class HomeViewModel(moods: MoodRepository, prefs: PreferencesStore) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(moods.entries, prefs.settings) { entries, settings ->
        val today = Dates.today()
        val byDay = entries.groupBy { Dates.localDate(it.createdAt) }
        val todayEntry = byDay[today]?.firstOrNull()
        HomeUiState(
            todayEntry = todayEntry,
            week = Dates.lastDays(7).map { d -> DaySummary(d, byDay[d]?.firstOrNull()?.emotion) },
            streakDays = streak(byDay.keys, today),
            recommended = InterventionCatalog.recommendedFor(todayEntry?.emotion),
            sustainedDistress = RiskDetector.hasSustainedDistress(entries.take(5)),
            examMode = settings.examMode,
            totalEntries = entries.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun streak(days: Set<LocalDate>, today: LocalDate): Int {
        val ordered = Dates.lastDays(60).reversed()
        val start = if (today in days) 0 else 1
        var count = 0
        for (d in ordered.drop(start)) if (d in days) count++ else break
        return count
    }
}
