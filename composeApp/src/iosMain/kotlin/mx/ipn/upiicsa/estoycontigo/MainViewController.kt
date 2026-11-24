package mx.ipn.upiicsa.estoycontigo

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import mx.ipn.upiicsa.estoycontigo.di.initKoin
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.ui.App
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

private val deepLink = MutableStateFlow<String?>(null)
private var started = false

/** Llamar una vez desde `iOSApp.init()` en Swift. */
fun startEstoyContigo() {
    if (started) return
    started = true
    initKoin(iosModule)
    KoinPlatform.getKoin().get<SyncScheduler>().requestSync()
}

/** Destino al tocar una notificación local ("mood" o "interventions"). */
fun openDestination(destination: String?) {
    deepLink.value = destination
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(deepLink = deepLink, onDeepLinkHandled = { deepLink.value = null })
}
