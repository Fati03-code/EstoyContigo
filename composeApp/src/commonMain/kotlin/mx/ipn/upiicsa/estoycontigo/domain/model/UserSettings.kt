package mx.ipn.upiicsa.estoycontigo.domain.model

enum class ThemeMode(val label: String) { SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro") }

data class UserSettings(
    val onboardingDone: Boolean = false,
    val disclaimerAccepted: Boolean = false,
    val accountStepDone: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val examMode: Boolean = false,
    val shakeEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
    val lastSyncAt: Long = 0L,
)

data class AuthSession(
    val uid: String,
    val email: String?,
    val idToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
)
