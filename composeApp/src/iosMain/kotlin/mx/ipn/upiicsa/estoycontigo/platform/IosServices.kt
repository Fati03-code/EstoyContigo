package mx.ipn.upiicsa.estoycontigo.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncManager
import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSData
import platform.Foundation.NSDateComponents
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.writeToFile
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.math.sqrt
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = if (isEmpty()) null else it.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
internal fun appSupportDir(): String {
    val dir = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true).first() as String
    NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
    // Diario, sesión y fotos fuera de los respaldos de iCloud/Finder (equivale a allowBackup=false).
    NSURL.fileURLWithPath(dir).setResourceValue(NSNumber(bool = true), NSURLIsExcludedFromBackupKey, null)
    return dir
}

/** Fotos en Application Support/photos: privado de la app y fuera de la carpeta compartida de Archivos. */
@OptIn(ExperimentalForeignApi::class)
class IosPhotoStorage : PhotoStorage {
    private val fm = NSFileManager.defaultManager
    private val dir: String by lazy {
        "${appSupportDir()}/photos".also { fm.createDirectoryAtPath(it, true, null, null) }
    }

    override suspend fun readBytes(path: String): ByteArray? = withContext(Dispatchers.IO) {
        NSData.dataWithContentsOfFile(path.removePrefix("file://"))?.toByteArray()
    }

    override suspend fun save(fileName: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val path = "$dir/$fileName"
        bytes.toNSData().writeToFile(path, atomically = true)
        path
    }

    override suspend fun delete(path: String) {
        withContext(Dispatchers.IO) { fm.removeItemAtPath(path.removePrefix("file://"), null) }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) { fm.removeItemAtPath(dir, null); fm.createDirectoryAtPath(dir, true, null, null) }
    }
}

@OptIn(ExperimentalForeignApi::class)
class IosMotionSensor : MotionSensor {
    private val manager = CMMotionManager()
    override val isAvailable: Boolean get() = manager.accelerometerAvailable

    // CoreMotion entrega la aceleración en g; ShakeDetector espera m/s².
    override fun shakes(): Flow<Unit> {
        if (!isAvailable) return emptyFlow()
        return callbackFlow {
            val detector = ShakeDetector { trySend(Unit) }
            val m = CMMotionManager().apply { accelerometerUpdateInterval = 1.0 / 50 }
            m.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
                data?.acceleration?.useContents {
                    val g = ShakeDetector.EARTH_GRAVITY
                    detector.onSample((x * g).toFloat(), (y * g).toFloat(), (z * g).toFloat(), nowMillis())
                }
            }
            awaitClose { m.stopAccelerometerUpdates() }
        }
    }

    override fun movement(): Flow<Float> {
        if (!isAvailable) return emptyFlow()
        return callbackFlow {
            var smoothed = 0f
            val gravity = DoubleArray(3)
            val m = CMMotionManager().apply { accelerometerUpdateInterval = 1.0 / 15 }
            m.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
                data?.acceleration?.useContents {
                    val v = doubleArrayOf(x, y, z)
                    var sum = 0.0
                    for (i in 0..2) {
                        gravity[i] = 0.9 * gravity[i] + 0.1 * v[i]
                        val d = (v[i] - gravity[i]) * ShakeDetector.EARTH_GRAVITY
                        sum += d * d
                    }
                    smoothed = 0.85f * smoothed + 0.15f * sqrt(sum).toFloat()
                    trySend(smoothed)
                }
            }
            awaitClose { m.stopAccelerometerUpdates() }
        }
    }
}

/** Ubicación puntual (no se guarda): una sola lectura con precisión de ~100 m. */
@OptIn(ExperimentalForeignApi::class)
class IosLocationProvider : LocationProvider {
    private var delegate: NSObject? = null

    override suspend fun currentLocation(): GeoPoint? {
        val manager = CLLocationManager()
        val status = manager.authorizationStatus
        if (status != kCLAuthorizationStatusAuthorizedWhenInUse && status != kCLAuthorizationStatusAuthorizedAlways) return null
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val d = object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                        val loc = didUpdateLocations.lastOrNull() as? CLLocation
                        if (cont.isActive) cont.resumeWith(Result.success(loc?.coordinate?.useContents { GeoPoint(latitude, longitude) }))
                    }
                    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                        if (cont.isActive) cont.resumeWith(Result.success(manager.location?.coordinate?.useContents { GeoPoint(latitude, longitude) }))
                    }
                }
                delegate = d
                manager.delegate = d
                manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
                manager.requestLocation()
                cont.invokeOnCancellation { manager.stopUpdatingLocation() }
            }
        }.also { delegate = null }
    }
}

private val examTips = listOf(
    "Antes de estudiar el siguiente tema, respira 1 minuto con Michi.",
    "Tu valor no se mide en una calificación. Haz una pausa corta.",
    "¿Ya tomaste agua hoy? Tu cerebro también lo necesita.",
    "Afloja hombros y mandíbula. Una pausa de 2 minutos ayuda a concentrarte.",
    "Si sientes que no puedes con todo, Orientación Juvenil de UPIICSA te puede acompañar.",
)

/** Notificaciones locales con UNUserNotificationCenter; el texto no revela emociones. */
class IosReminderScheduler : ReminderScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    private fun schedule(id: String, title: String, body: String, hour: Int, minute: Int, repeats: Boolean, day: Int? = null, month: Int? = null) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setUserInfo(mapOf("destination" to if (id.startsWith(DAILY)) "mood" else "interventions"))
        }
        val comps = NSDateComponents().apply {
            this.hour = hour.toLong(); this.minute = minute.toLong()
            day?.let { this.day = it.toLong() }; month?.let { this.month = it.toLong() }
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(comps, repeats)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id, content, trigger), null)
    }

    override fun scheduleDaily(hour: Int, minute: Int) {
        cancelDaily()
        schedule(DAILY, "EstoyContigo", "¿Cómo te sientes hoy?", hour, minute, repeats = true)
    }

    override fun cancelDaily() = center.removePendingNotificationRequestsWithIdentifiers(listOf(DAILY))

    override fun setExamMode(enabled: Boolean) {
        val ids = (0 until EXAM_DAYS).map { "$EXAM-$it" }
        center.removePendingNotificationRequestsWithIdentifiers(ids)
        if (!enabled) return
        val dayMs = 86_400_000L
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        repeat(EXAM_DAYS) { i ->
            val date = kotlin.time.Instant.fromEpochMilliseconds(now + (i + 1) * dayMs)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            schedule("$EXAM-$i", "Pausa de exámenes", examTips[i % examTips.size], 12, 30, repeats = false,
                day = date.dayOfMonth, month = date.monthNumber)
        }
    }

    private companion object {
        const val DAILY = "daily-reminder"
        const val EXAM = "exam-tip"
        const val EXAM_DAYS = 14
    }
}

/**
 * iOS no tiene WorkManager: se sincroniza al abrir la app y al recuperar conexión
 * (AppViewModel observa ConnectivityMonitor y llama a requestSync()).
 */
class IosSyncScheduler : SyncScheduler, KoinComponent {
    private val sync: SyncManager by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun requestSync() { scope.launch { runCatching { sync.syncPending() } } }
    override fun schedulePeriodic() = Unit
    override fun cancelAll() = Unit
}

class IosConnectivityMonitor : ConnectivityMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path -> trySend(nw_path_get_status(path) == nw_path_status_satisfied) }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }.distinctUntilChanged()
}

class IosExternalActions : ExternalActions {
    private fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any?>(), null)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun enc(s: String): String =
        (s as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: s

    override fun dial(phone: String) = open("tel:${phone.filter { it.isDigit() || it == '*' || it == '+' }}")
    override fun openWhatsApp(phone: String) = open("https://wa.me/${phone.filter { it.isDigit() }}")
    override fun sendEmail(email: String) = open("mailto:$email")
    override fun openMap(latitude: Double, longitude: Double, label: String) =
        open("http://maps.apple.com/?ll=$latitude,$longitude&q=${enc(label)}")
    override fun openUrl(url: String) = open(url)
    override fun openAppSettings() = open(UIApplicationOpenSettingsURLString)
}

class IosHaptics : Haptics {
    override fun tick() = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
}
