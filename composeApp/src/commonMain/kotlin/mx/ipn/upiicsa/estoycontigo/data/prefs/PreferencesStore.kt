package mx.ipn.upiicsa.estoycontigo.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mx.ipn.upiicsa.estoycontigo.domain.model.AuthSession
import mx.ipn.upiicsa.estoycontigo.domain.model.ThemeMode
import mx.ipn.upiicsa.estoycontigo.domain.model.UserSettings
import okio.Path.Companion.toPath

const val DATASTORE_FILE = "estoycontigo.preferences_pb"

const val DATA_STORE_FILE = "estoycontigo.preferences_pb"

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

/** Preferencias del usuario (DataStore). Nunca guarda contenido emocional. */
class PreferencesStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val disclaimerAccepted = booleanPreferencesKey("disclaimer_accepted")
        val accountStepDone = booleanPreferencesKey("account_step_done")
        val reminderEnabled = booleanPreferencesKey("reminder_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val examMode = booleanPreferencesKey("exam_mode")
        val shakeEnabled = booleanPreferencesKey("shake_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val themeMode = stringPreferencesKey("theme_mode")
        val trustedName = stringPreferencesKey("trusted_contact_name")
        val trustedPhone = stringPreferencesKey("trusted_contact_phone")
        val lastSyncAt = longPreferencesKey("last_sync_at")
        val directorySeedVersion = intPreferencesKey("directory_seed_version")

        val authUid = stringPreferencesKey("auth_uid")
        val authEmail = stringPreferencesKey("auth_email")
        val authIdToken = stringPreferencesKey("auth_id_token")
        val authRefreshToken = stringPreferencesKey("auth_refresh_token")
        val authExpiresAt = longPreferencesKey("auth_expires_at")
    }

    val settings: Flow<UserSettings> = dataStore.data.map { p ->
        UserSettings(
            onboardingDone = p[Keys.onboardingDone] ?: false,
            disclaimerAccepted = p[Keys.disclaimerAccepted] ?: false,
            accountStepDone = p[Keys.accountStepDone] ?: false,
            reminderEnabled = p[Keys.reminderEnabled] ?: false,
            reminderHour = p[Keys.reminderHour] ?: 20,
            reminderMinute = p[Keys.reminderMinute] ?: 0,
            examMode = p[Keys.examMode] ?: false,
            shakeEnabled = p[Keys.shakeEnabled] ?: true,
            hapticsEnabled = p[Keys.hapticsEnabled] ?: true,
            themeMode = ThemeMode.entries.firstOrNull { it.name == p[Keys.themeMode] } ?: ThemeMode.SYSTEM,
            trustedContactName = p[Keys.trustedName] ?: "",
            trustedContactPhone = p[Keys.trustedPhone] ?: "",
            lastSyncAt = p[Keys.lastSyncAt] ?: 0L,
        )
    }

    val session: Flow<AuthSession?> = dataStore.data.map { p ->
        val uid = p[Keys.authUid]
        val token = p[Keys.authIdToken]
        val refresh = p[Keys.authRefreshToken]
        if (uid.isNullOrBlank() || token.isNullOrBlank() || refresh.isNullOrBlank()) null
        else AuthSession(uid, p[Keys.authEmail], token, refresh, p[Keys.authExpiresAt] ?: 0L)
    }

    suspend fun currentSettings(): UserSettings = settings.first()
    suspend fun currentSession(): AuthSession? = session.first()

    suspend fun completeOnboarding() = dataStore.edit {
        it[Keys.onboardingDone] = true
        it[Keys.disclaimerAccepted] = true
    }

    suspend fun setAccountStepDone(done: Boolean) = dataStore.edit { it[Keys.accountStepDone] = done }
    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) = dataStore.edit {
        it[Keys.reminderEnabled] = enabled
        it[Keys.reminderHour] = hour
        it[Keys.reminderMinute] = minute
    }
    suspend fun setExamMode(enabled: Boolean) = dataStore.edit { it[Keys.examMode] = enabled }
    suspend fun setShakeEnabled(enabled: Boolean) = dataStore.edit { it[Keys.shakeEnabled] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = dataStore.edit { it[Keys.hapticsEnabled] = enabled }
    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[Keys.themeMode] = mode.name }
    suspend fun setTrustedContact(name: String, phone: String) = dataStore.edit {
        it[Keys.trustedName] = name
        it[Keys.trustedPhone] = phone
    }
    suspend fun setLastSyncAt(millis: Long) = dataStore.edit { it[Keys.lastSyncAt] = millis }

    suspend fun directorySeedVersion(): Int = dataStore.data.first()[Keys.directorySeedVersion] ?: 0
    suspend fun setDirectorySeedVersion(v: Int) = dataStore.edit { it[Keys.directorySeedVersion] = v }

    suspend fun saveSession(session: AuthSession) = dataStore.edit {
        it[Keys.authUid] = session.uid
        session.email?.let { email -> it[Keys.authEmail] = email } ?: it.remove(Keys.authEmail)
        it[Keys.authIdToken] = session.idToken
        it[Keys.authRefreshToken] = session.refreshToken
        it[Keys.authExpiresAt] = session.expiresAtMillis
    }

    suspend fun clearSession() = dataStore.edit {
        it.remove(Keys.authUid)
        it.remove(Keys.authEmail)
        it.remove(Keys.authIdToken)
        it.remove(Keys.authRefreshToken)
        it.remove(Keys.authExpiresAt)
    }

    suspend fun clearAll() = dataStore.edit { it.clear() }
}
