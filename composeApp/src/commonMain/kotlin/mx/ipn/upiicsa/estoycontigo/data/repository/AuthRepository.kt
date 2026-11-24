package mx.ipn.upiicsa.estoycontigo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.remote.FirebaseAuthApi
import mx.ipn.upiicsa.estoycontigo.data.remote.RemoteException
import mx.ipn.upiicsa.estoycontigo.data.remote.authErrorToSpanish
import mx.ipn.upiicsa.estoycontigo.domain.model.AuthSession
import mx.ipn.upiicsa.estoycontigo.platform.nowMillis

class AuthRepository(
    private val api: FirebaseAuthApi,
    private val prefs: PreferencesStore,
    private val config: FirebaseConfig,
) {
    private val refreshMutex = Mutex()

    val isCloudAvailable: Boolean get() = config.isConfigured
    val session: Flow<AuthSession?> = prefs.session

    suspend fun signIn(email: String, password: String): Result<AuthSession> = runAuth {
        val r = api.signIn(email.trim(), password)
        AuthSession(r.localId, r.email ?: email.trim(), r.idToken, r.refreshToken, expiry(r.expiresIn))
    }

    suspend fun signUp(email: String, password: String): Result<AuthSession> = runAuth {
        val r = api.signUp(email.trim(), password)
        AuthSession(r.localId, r.email ?: email.trim(), r.idToken, r.refreshToken, expiry(r.expiresIn))
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching { api.sendPasswordReset(email.trim()) }
        .recoverCatching { throw IllegalStateException(authErrorToSpanish(it.message ?: "")) }

    /** Devuelve un ID token vigente, refrescándolo si está por expirar. */
    suspend fun validSession(): AuthSession? = refreshMutex.withLock {
        val current = prefs.currentSession() ?: return null
        if (current.expiresAtMillis - nowMillis() > 60_000) return current
        return try {
            val r = api.refresh(current.refreshToken)
            current.copy(idToken = r.idToken, refreshToken = r.refreshToken, expiresAtMillis = expiry(r.expiresIn))
                .also { prefs.saveSession(it) }
        } catch (e: RemoteException) {
            if (e.code in 400..403) prefs.clearSession()
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteRemoteAccount(idToken: String) = api.deleteAccount(idToken)

    suspend fun signOut() = refreshMutex.withLock { prefs.clearSession() }

    private suspend fun runAuth(block: suspend () -> AuthSession): Result<AuthSession> =
        try {
            if (!config.isConfigured) error("La nube no está configurada en esta versión de la app.")
            val s = block()
            prefs.saveSession(s)
            Result.success(s)
        } catch (e: RemoteException) {
            Result.failure(IllegalStateException(authErrorToSpanish(e.message)))
        } catch (e: IllegalStateException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(authErrorToSpanish(e.message ?: "")))
        }

    private fun expiry(expiresIn: String) = nowMillis() + (expiresIn.toLongOrNull() ?: 3600L) * 1000
}
