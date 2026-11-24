package mx.ipn.upiicsa.estoycontigo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.flow.MutableStateFlow
import mx.ipn.upiicsa.estoycontigo.ui.App
import mx.ipn.upiicsa.estoycontigo.work.Notifications

class MainActivity : ComponentActivity() {
    private val deepLink = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) deepLink.value = intent?.getStringExtra(Notifications.EXTRA_DESTINATION)
        setContent {
            App(deepLink = deepLink, onDeepLinkHandled = { deepLink.value = null })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLink.value = intent.getStringExtra(Notifications.EXTRA_DESTINATION)
    }
}
