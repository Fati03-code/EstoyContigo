package mx.ipn.upiicsa.estoycontigo.platform

import kotlinx.coroutines.flow.Flow
import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint

/** Guarda fotos comprimidas en el almacenamiento privado de la app. */
interface PhotoStorage {
    suspend fun readBytes(path: String): ByteArray?
    suspend fun save(fileName: String, bytes: ByteArray): String
    suspend fun delete(path: String)
    suspend fun deleteAll()
}

/** Acelerómetro: agitar para registrar y nivel de movimiento durante la respiración. */
interface MotionSensor {
    val isAvailable: Boolean
    fun shakes(): Flow<Unit>
    /** Magnitud de movimiento (m/s² sin gravedad), suavizada. */
    fun movement(): Flow<Float>
}

interface LocationProvider {
    suspend fun currentLocation(): GeoPoint?
}

interface ReminderScheduler {
    fun scheduleDaily(hour: Int, minute: Int)
    fun cancelDaily()
    fun setExamMode(enabled: Boolean)
}

interface SyncScheduler {
    /** Pide sincronizar en cuanto haya red (WorkManager en Android, NWPathMonitor en iOS). */
    fun requestSync()
    fun schedulePeriodic()
    fun cancelAll()
}

interface ConnectivityMonitor {
    val isOnline: Flow<Boolean>
}

interface ExternalActions {
    fun dial(phone: String)
    fun openWhatsApp(phone: String)
    fun sendEmail(email: String)
    fun openMap(latitude: Double, longitude: Double, label: String)
    fun openUrl(url: String)
    fun openAppSettings()
}

interface Haptics {
    fun tick()
}

fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
