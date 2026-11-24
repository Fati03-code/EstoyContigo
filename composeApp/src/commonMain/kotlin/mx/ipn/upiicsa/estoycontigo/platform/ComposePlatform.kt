package mx.ipn.upiicsa.estoycontigo.platform

import androidx.compose.runtime.Composable

enum class AppPermission { CAMERA, LOCATION, NOTIFICATIONS }

enum class PermissionStatus { GRANTED, DENIED, PERMANENTLY_DENIED }

interface PermissionRequester {
    val status: PermissionStatus
    fun request()
}

/** Solicita un permiso en tiempo de ejecución; `onResult` recibe si fue concedido. */
@Composable
expect fun rememberPermissionRequester(
    permission: AppPermission,
    onResult: (Boolean) -> Unit = {},
): PermissionRequester

/** Abre la cámara; `onPhoto` recibe la ruta local de la foto comprimida o null si se canceló. */
@Composable
expect fun rememberCameraLauncher(onPhoto: (String?) -> Unit): () -> Unit

/** Para que Coil cargue fotos locales en ambas plataformas. */
fun photoModel(path: String): String = if (path.startsWith("file://")) path else "file://$path"
