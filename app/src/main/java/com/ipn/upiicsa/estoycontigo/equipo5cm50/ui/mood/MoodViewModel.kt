package com.ipn.upiicsa.estoycontigo.equipo5cm50.ui.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.local.MoodEntry
import com.ipn.upiicsa.estoycontigo.equipo5cm50.data.repository.MoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoodViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    val moods: StateFlow<List<MoodEntry>> = repository.allMoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addMood(moodLevel: Int, emotionLabel: String, note: String) {
        if (emotionLabel.isBlank()) return
        viewModelScope.launch {
            repository.saveMood(
                moodLevel = moodLevel,
                emotionLabel = emotionLabel,
                note = note
            )
        }
    }

    class Factory(private val repository: MoodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
                return MoodViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}