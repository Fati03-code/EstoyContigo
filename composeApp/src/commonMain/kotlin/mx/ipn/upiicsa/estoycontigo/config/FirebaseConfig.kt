package mx.ipn.upiicsa.estoycontigo.config

/**
 * Configuración de Firebase (Auth + Firestore + Storage) consumida vía REST,
 * lo que permite usar el mismo cliente en Android e iOS.
 * Los valores se generan desde local.properties (no se versionan).
 */
data class FirebaseConfig(
    val apiKey: String,
    val projectId: String,
    val storageBucket: String,
    val emulatorHost: String,
) {
    val useEmulator: Boolean get() = emulatorHost.isNotBlank()
    val isConfigured: Boolean get() = projectId.isNotBlank() && (apiKey.isNotBlank() || useEmulator)

    private val key: String get() = apiKey.ifBlank { "emulator-key" }
    private val bucket: String get() = storageBucket.ifBlank { "$projectId.appspot.com" }

    fun authUrl(method: String): String =
        if (useEmulator) "http://$emulatorHost:9099/identitytoolkit.googleapis.com/v1/accounts:$method?key=$key"
        else "https://identitytoolkit.googleapis.com/v1/accounts:$method?key=$key"

    fun tokenUrl(): String =
        if (useEmulator) "http://$emulatorHost:9099/securetoken.googleapis.com/v1/token?key=$key"
        else "https://securetoken.googleapis.com/v1/token?key=$key"

    fun firestoreDocumentsUrl(): String =
        if (useEmulator) "http://$emulatorHost:8080/v1/projects/$projectId/databases/(default)/documents"
        else "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    fun storageObjectsUrl(): String =
        if (useEmulator) "http://$emulatorHost:9199/v0/b/$bucket/o"
        else "https://firebasestorage.googleapis.com/v0/b/$bucket/o"

    companion object {
        fun fromBuild(): FirebaseConfig = FirebaseConfig(
            apiKey = GeneratedFirebaseConfig.API_KEY,
            projectId = GeneratedFirebaseConfig.PROJECT_ID,
            storageBucket = GeneratedFirebaseConfig.STORAGE_BUCKET,
            emulatorHost = GeneratedFirebaseConfig.EMULATOR_HOST,
        )
    }
}
