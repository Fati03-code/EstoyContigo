package mx.ipn.upiicsa.estoycontigo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.ipn.upiicsa.estoycontigo.data.local.DirectorySeed
import mx.ipn.upiicsa.estoycontigo.data.local.dao.DirectoryDao
import mx.ipn.upiicsa.estoycontigo.data.local.toDomain
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint
import mx.ipn.upiicsa.estoycontigo.domain.model.SupportService
import mx.ipn.upiicsa.estoycontigo.domain.usecase.GeoDistance

class DirectoryRepository(private val dao: DirectoryDao, private val prefs: PreferencesStore) {

    val services: Flow<List<SupportService>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Precarga el directorio en Room para que esté disponible sin conexión. */
    suspend fun ensureSeeded() {
        if (prefs.directorySeedVersion() < DirectorySeed.SEED_VERSION || dao.count() == 0) {
            dao.replaceAll(DirectorySeed.services)
            prefs.setDirectorySeedVersion(DirectorySeed.SEED_VERSION)
        }
    }

    companion object {
        fun sortByDistance(services: List<SupportService>, from: GeoPoint): List<SupportService> =
            services.map { s ->
                if (s.latitude != null && s.longitude != null) {
                    s.copy(distanceKm = GeoDistance.haversineKm(from, GeoPoint(s.latitude, s.longitude)))
                } else s
            }.sortedWith(compareBy<SupportService> { it.distanceKm == null }.thenBy { it.distanceKm ?: 0.0 })
    }
}
