package mx.ipn.upiicsa.estoycontigo.domain.usecase

import mx.ipn.upiicsa.estoycontigo.domain.model.Intensity
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodDraft

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val errors: Map<String, String>) : ValidationResult
}

object MoodValidator {
    const val FIELD_EMOTION = "emotion"
    const val FIELD_INTENSITY = "intensity"
    const val FIELD_NOTE = "note"
    const val NOTE_MAX_LENGTH = 500

    fun validate(draft: MoodDraft): ValidationResult {
        val errors = buildMap {
            if (draft.emotion == null) put(FIELD_EMOTION, "Elige cómo te sientes para poder guardar")
            if (draft.intensity !in Intensity.MIN..Intensity.MAX) put(FIELD_INTENSITY, "La intensidad debe estar entre 1 y 5")
            if (draft.note.length > NOTE_MAX_LENGTH) put(FIELD_NOTE, "Máximo $NOTE_MAX_LENGTH caracteres")
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

object CredentialsValidator {
    const val FIELD_EMAIL = "email"
    const val FIELD_PASSWORD = "password"
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val PASSWORD_MIN = 8

    fun validate(email: String, password: String, isSignUp: Boolean): ValidationResult {
        val errors = buildMap {
            when {
                email.isBlank() -> put(FIELD_EMAIL, "Campo obligatorio")
                !emailRegex.matches(email.trim()) -> put(FIELD_EMAIL, "Escribe un correo válido (ej. nombre@alumno.ipn.mx)")
            }
            when {
                password.isBlank() -> put(FIELD_PASSWORD, "Campo obligatorio")
                isSignUp && password.length < PASSWORD_MIN -> put(FIELD_PASSWORD, "Usa al menos $PASSWORD_MIN caracteres")
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    fun isValidPhone(phone: String): Boolean = phone.filter { it.isDigit() }.length in 8..15
}
