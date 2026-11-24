package mx.ipn.upiicsa.estoycontigo.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File
import java.util.UUID

private fun AppPermission.androidPermissions(): Array<String> = when (this) {
    AppPermission.CAMERA -> arrayOf(Manifest.permission.CAMERA)
    AppPermission.LOCATION -> arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    AppPermission.NOTIFICATIONS ->
        if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray()
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun isGranted(context: Context, permission: AppPermission): Boolean {
    val perms = permission.androidPermissions()
    return perms.isEmpty() || perms.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
}

@Composable
actual fun rememberPermissionRequester(
    permission: AppPermission,
    onResult: (Boolean) -> Unit,
): PermissionRequester {
    val context = LocalContext.current
    var status by remember { mutableStateOf(if (isGranted(context, permission)) PermissionStatus.GRANTED else PermissionStatus.DENIED) }
    var askedBefore by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.values.any { it } || isGranted(context, permission)
        val activity = context.findActivity()
        val canAskAgain = permission.androidPermissions().any { p ->
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, p)
        }
        status = when {
            granted -> PermissionStatus.GRANTED
            askedBefore && !canAskAgain -> PermissionStatus.PERMANENTLY_DENIED
            !canAskAgain -> PermissionStatus.PERMANENTLY_DENIED
            else -> PermissionStatus.DENIED
        }
        askedBefore = true
        onResult(granted)
    }

    // Al volver de Ajustes se actualiza el estado.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (isGranted(context, permission)) status = PermissionStatus.GRANTED
    }

    return remember(launcher, status) {
        object : PermissionRequester {
            override val status: PermissionStatus = status
            override fun request() {
                val perms = permission.androidPermissions()
                if (perms.isEmpty() || isGranted(context, permission)) {
                    onResult(true)
                } else {
                    launcher.launch(perms)
                }
            }
        }
    }
}

@Composable
actual fun rememberCameraLauncher(onPhoto: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val storage = koinInject<PhotoStorage>() as AndroidPhotoStorage
    val scope = rememberCoroutineScope()
    var pendingFile by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingFile
        pendingFile = null
        if (!success || path == null) {
            path?.let { File(it).delete() }
            onPhoto(null)
        } else {
            scope.launch { onPhoto(storage.compressFrom(File(path), "${UUID.randomUUID()}.jpg")) }
        }
    }

    return {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        pendingFile = file.absolutePath
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        try {
            launcher.launch(uri)
        } catch (_: Exception) {
            pendingFile = null
            onPhoto(null)
        }
    }
}
