package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.AuthRepository
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncManager
import mx.ipn.upiicsa.estoycontigo.domain.usecase.CredentialsValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.ValidationResult
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler

class OnboardingViewModel(private val prefs: PreferencesStore) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.completeOnboarding()
            onDone()
        }
    }
}

data class AuthUiState(
    val isSignUp: Boolean = true,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val message: String? = null,
    val isLoading: Boolean = false,
    val cloudAvailable: Boolean = true,
    val signedInEmail: String? = null,
)

class AuthViewModel(
    private val auth: AuthRepository,
    private val prefs: PreferencesStore,
    private val sync: SyncManager,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(cloudAvailable = auth.isCloudAvailable))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            auth.session.collect { s -> _state.update { it.copy(signedInEmail = s?.email) } }
        }
    }

    fun onEmail(v: String) = _state.update { it.copy(email = v, emailError = null, message = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, passwordError = null, message = null) }
    fun toggleMode() = _state.update { it.copy(isSignUp = !it.isSignUp, message = null, emailError = null, passwordError = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        val validation = CredentialsValidator.validate(s.email, s.password, s.isSignUp)
        if (validation is ValidationResult.Invalid) {
            _state.update { it.copy(emailError = validation.errors["email"], passwordError = validation.errors["password"]) }
            return
        }
        _state.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val result = if (s.isSignUp) auth.signUp(s.email, s.password) else auth.signIn(s.email, s.password)
            result.onSuccess {
                prefs.setAccountStepDone(true)
                runCatching { sync.restoreFromCloud() }
                syncScheduler.requestSync()
                _state.update { it.copy(isLoading = false, password = "") }
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, message = e.message) }
            }
        }
    }

    fun resetPassword() {
        val email = _state.value.email
        if (CredentialsValidator.validate(email, "xxxxxxxx", false) is ValidationResult.Invalid) {
            _state.update { it.copy(emailError = "Escribe tu correo para enviarte el enlace.") }
            return
        }
        viewModelScope.launch {
            val r = auth.sendPasswordReset(email)
            _state.update {
                it.copy(message = r.fold({ "Te enviamos un correo para restablecer tu contraseña." }, { e -> e.message }))
            }
        }
    }

    fun continueOffline(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setAccountStepDone(true)
            onDone()
        }
    }
}
