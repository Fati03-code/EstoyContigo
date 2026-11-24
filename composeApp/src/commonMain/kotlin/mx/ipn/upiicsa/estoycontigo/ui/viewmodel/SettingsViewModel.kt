package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.AuthRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.DirectoryRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncManager
import mx.ipn.upiicsa.estoycontigo.domain.model.ThemeMode
import mx.ipn.upiicsa.estoycontigo.domain.model.UserSettings
import mx.ipn.upiicsa.estoycontigo.domain.usecase.CredentialsValidator
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.MotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.ReminderScheduler
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val email: String? = null,
    val cloudAvailable: Boolean = false,
    val accelerometerAvailable: Boolean = true,
)

class SettingsViewModel(
    private val prefs: PreferencesStore,
    private val reminders: ReminderScheduler,
    private val auth: AuthRepository,
    private val moods: MoodRepository,
    private val directory: DirectoryRepository,
    private val sync: SyncManager,
    private val syncScheduler: SyncScheduler,
    motion: MotionSensor,
    val actions: ExternalActions,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(prefs.settings, auth.session) { s, session ->
        SettingsUiState(s, session?.email, auth.isCloudAvailable, motion.isAvailable)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun clearMessage() { _message.value = null }

    fun setReminder(enabled: Boolean, hour: Int = state.value.settings.reminderHour, minute: Int = state.value.settings.reminderMinute) {
        viewModelScope.launch {
            prefs.setReminder(enabled, hour, minute)
            if (enabled) reminders.scheduleDaily(hour, minute) else reminders.cancelDaily()
        }
    }

    fun setExamMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setExamMode(enabled)
        reminders.setExamMode(enabled)
    }

    fun setShake(enabled: Boolean) = viewModelScope.launch { prefs.setShakeEnabled(enabled) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { prefs.setHapticsEnabled(enabled) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }

    fun saveTrustedContact(name: String, phone: String): Boolean {
        if (phone.isNotBlank() && !CredentialsValidator.isValidPhone(phone)) return false
        viewModelScope.launch {
            prefs.setTrustedContact(name.trim(), phone.filter { it.isDigit() || it == '+' })
            _message.value = "Contacto de confianza guardado"
        }
        return true
    }

    fun signOut() = viewModelScope.launch {
        auth.signOut()
        _message.value = "Sesión cerrada. Tus registros siguen en este teléfono."
    }

    /** Derecho de cancelación: borra registros, fotos y preferencias (y la cuenta en la nube si se pide). */
    fun deleteAllData(includeCloud: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        var cloudOk = true
        if (includeCloud && auth.isCloudAvailable) {
            cloudOk = runCatching { sync.deleteAllRemote() }.getOrDefault(false)
        }
        syncScheduler.cancelAll()
        reminders.cancelDaily()
        reminders.setExamMode(false)
        moods.deleteAllLocal()
        prefs.clearAll()
        directory.ensureSeeded()
        _message.value = if (cloudOk) "Se eliminaron todos tus datos." else
            "Se borraron los datos del teléfono. No se pudo borrar la nube: revisa tu conexión e inténtalo de nuevo."
        onDone()
    }
}
