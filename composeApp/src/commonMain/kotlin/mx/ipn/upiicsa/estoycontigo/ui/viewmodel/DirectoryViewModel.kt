package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.repository.DirectoryRepository
import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory
import mx.ipn.upiicsa.estoycontigo.domain.model.SupportService
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.LocationProvider

enum class LocationState { IDLE, LOADING, FOUND, UNAVAILABLE }

data class DirectoryUiState(
    val services: List<SupportService> = emptyList(),
    val filter: ServiceCategory? = null,
    val location: GeoPoint? = null,
    val locationState: LocationState = LocationState.IDLE,
    val trustedName: String = "",
    val trustedPhone: String = "",
)

class DirectoryViewModel(
    repository: DirectoryRepository,
    prefs: PreferencesStore,
    private val locationProvider: LocationProvider,
    val actions: ExternalActions,
) : ViewModel() {

    private val filter = MutableStateFlow<ServiceCategory?>(null)
    private val location = MutableStateFlow<GeoPoint?>(null)
    private val locationState = MutableStateFlow(LocationState.IDLE)

    val state: StateFlow<DirectoryUiState> = combine(
        repository.services, filter, location, locationState, prefs.settings,
    ) { services, f, loc, locState, settings ->
        val filtered = services.filter { f == null || it.category == f }
        DirectoryUiState(
            services = if (loc != null) DirectoryRepository.sortByDistance(filtered, loc) else filtered,
            filter = f,
            location = loc,
            locationState = locState,
            trustedName = settings.trustedContactName,
            trustedPhone = settings.trustedContactPhone,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DirectoryUiState())

    fun setFilter(category: ServiceCategory?) = filter.update { category }

    /** Se llama después de que la persona concede el permiso de ubicación. */
    fun locate() {
        locationState.value = LocationState.LOADING
        viewModelScope.launch {
            val point = locationProvider.currentLocation()
            location.value = point
            locationState.value = if (point != null) LocationState.FOUND else LocationState.UNAVAILABLE
        }
    }

    fun locationDenied() {
        locationState.value = LocationState.UNAVAILABLE
    }
}
