package mx.ipn.upiicsa.estoycontigo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmailPasswordRequest(val email: String, val password: String, val returnSecureToken: Boolean = true)

@Serializable
data class AuthResponse(
    val idToken: String,
    val refreshToken: String,
    val localId: String,
    val email: String? = null,
    val expiresIn: String = "3600",
)

@Serializable
data class RefreshResponse(
    @SerialName("id_token") val idToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id") val userId: String,
    @SerialName("expires_in") val expiresIn: String = "3600",
)

@Serializable
data class PasswordResetRequest(val email: String, val requestType: String = "PASSWORD_RESET")

@Serializable
data class IdTokenRequest(val idToken: String)

@Serializable
data class FirebaseErrorEnvelope(val error: FirebaseError? = null)

@Serializable
data class FirebaseError(val code: Int = 0, val message: String = "")

/** DTO del documento users/{uid}/moods/{id} en Firestore. */
data class MoodDocument(
    val id: String,
    val emotion: String,
    val intensity: Int,
    val note: String,
    val tags: String,
    val createdAt: Long,
    val updatedAt: Long,
    val photoPath: String?,
)
