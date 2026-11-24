package mx.ipn.upiicsa.estoycontigo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.math.max
import kotlin.uuid.Uuid

private fun onMain(block: () -> Unit) = dispatch_async(dispatch_get_main_queue(), block)

/** En iOS, una vez denegado el permiso solo puede cambiarse en Ajustes: se reporta como PERMANENTLY_DENIED. */
private class IosPermissionRequester(
    private val permission: AppPermission,
    private val onResult: (Boolean) -> Unit,
) : PermissionRequester {
    var current by mutableStateOf(PermissionStatus.DENIED)
    private var locationManager: CLLocationManager? = null
    private var awaitingLocation = false
    private val locationDelegate = LocationAuthDelegate { onLocationAuthorization(it) }

    override val status: PermissionStatus get() = current

    fun refresh() {
        when (permission) {
            AppPermission.CAMERA -> current = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
                AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
                AVAuthorizationStatusNotDetermined -> PermissionStatus.DENIED
                else -> PermissionStatus.PERMANENTLY_DENIED
            }
            AppPermission.LOCATION -> current = locationStatus(CLLocationManager().authorizationStatus)
            AppPermission.NOTIFICATIONS -> UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { s ->
                val st = when (s?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral -> PermissionStatus.GRANTED
                    UNAuthorizationStatusNotDetermined -> PermissionStatus.DENIED
                    else -> PermissionStatus.PERMANENTLY_DENIED
                }
                onMain { current = st }
            }
        }
    }

    private fun locationStatus(s: Int) = when (s) {
        kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.GRANTED
        kCLAuthorizationStatusNotDetermined -> PermissionStatus.DENIED
        else -> PermissionStatus.PERMANENTLY_DENIED
    }

    private fun finish(granted: Boolean) = onMain {
        refresh()
        current = if (granted) PermissionStatus.GRANTED else PermissionStatus.PERMANENTLY_DENIED
        onResult(granted)
    }

    override fun request() {
        when (permission) {
            AppPermission.CAMERA -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted -> finish(granted) }
            AppPermission.NOTIFICATIONS -> UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            ) { granted, _ -> finish(granted) }
            AppPermission.LOCATION -> {
                val m = CLLocationManager()
                if (m.authorizationStatus != kCLAuthorizationStatusNotDetermined) {
                    finish(locationStatus(m.authorizationStatus) == PermissionStatus.GRANTED)
                    return
                }
                awaitingLocation = true
                locationManager = m
                m.delegate = locationDelegate
                m.requestWhenInUseAuthorization()
            }
        }
    }

    private fun onLocationAuthorization(s: Int) {
        if (awaitingLocation && s != kCLAuthorizationStatusNotDetermined) {
            awaitingLocation = false
            finish(locationStatus(s) == PermissionStatus.GRANTED)
        }
    }
}

private class LocationAuthDelegate(private val onChange: (Int) -> Unit) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) = onChange(manager.authorizationStatus)
}

@Composable
actual fun rememberPermissionRequester(permission: AppPermission, onResult: (Boolean) -> Unit): PermissionRequester {
    val callback by rememberUpdatedState(onResult)
    val requester = remember(permission) { IosPermissionRequester(permission) { callback(it) } }
    DisposableEffect(requester) {
        requester.refresh()
        onDispose { }
    }
    return requester
}

private fun topViewController(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

/** Redimensiona a máx. 1280 px y normaliza la orientación; el JPEG resultante no incluye metadatos GPS/EXIF. */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.compressedJpeg(maxSide: Double = 1280.0): ByteArray? {
    val (w, h) = size.useContents { width to height }
    val scale = minOf(1.0, maxSide / max(w, h))
    val target = CGSizeMake(w * scale, h * scale)
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { setScale(1.0) }
    val resized = UIGraphicsImageRenderer(target, format).imageWithActions { _ ->
        drawInRect(CGRectMake(0.0, 0.0, w * scale, h * scale))
    }
    return UIImageJPEGRepresentation(resized, 0.8)?.toByteArray()
}

private class PickerDelegate(private val onImage: (UIImage?) -> Unit) :
    NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true, null)
        onImage(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
        onImage(null)
    }
}

@Composable
actual fun rememberCameraLauncher(onPhoto: (String?) -> Unit): () -> Unit {
    val storage = koinInject<PhotoStorage>()
    val scope = rememberCoroutineScope()
    val callback by rememberUpdatedState(onPhoto)
    val delegate = remember {
        PickerDelegate { image ->
            if (image == null) { callback(null); return@PickerDelegate }
            scope.launch {
                val bytes = image.compressedJpeg()
                callback(bytes?.let { storage.save("${Uuid.random()}.jpg", it) })
            }
        }
    }
    return remember {
        {
            val picker = UIImagePickerController()
            // El simulador no tiene cámara: se usa la fototeca como alternativa.
            picker.sourceType = if (UIImagePickerController.isSourceTypeAvailable(
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
            ) UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            else UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}
