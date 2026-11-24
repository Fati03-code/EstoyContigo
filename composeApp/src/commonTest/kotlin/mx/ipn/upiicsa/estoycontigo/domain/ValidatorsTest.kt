package mx.ipn.upiicsa.estoycontigo.domain

import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.MoodDraft
import mx.ipn.upiicsa.estoycontigo.domain.usecase.CredentialsValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.MoodValidator
import mx.ipn.upiicsa.estoycontigo.domain.usecase.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ValidatorsTest {
    private fun draft(emotion: Emotion? = Emotion.CALMA, intensity: Int = 3, note: String = "") =
        MoodDraft(emotion, intensity, note, emptySet(), null)

    @Test fun moodWithoutEmotionIsInvalid() {
        val r = assertIs<ValidationResult.Invalid>(MoodValidator.validate(draft(emotion = null)))
        assertTrue(MoodValidator.FIELD_EMOTION in r.errors)
    }

    @Test fun moodIntensityOutOfRangeIsInvalid() {
        assertIs<ValidationResult.Invalid>(MoodValidator.validate(draft(intensity = 0)))
        assertIs<ValidationResult.Invalid>(MoodValidator.validate(draft(intensity = 6)))
    }

    @Test fun noteLongerThanLimitIsInvalid() {
        val r = assertIs<ValidationResult.Invalid>(MoodValidator.validate(draft(note = "a".repeat(501))))
        assertEquals(setOf(MoodValidator.FIELD_NOTE), r.errors.keys)
        assertEquals(ValidationResult.Valid, MoodValidator.validate(draft(note = "a".repeat(500))))
    }

    @Test fun validMoodPasses() = assertEquals(ValidationResult.Valid, MoodValidator.validate(draft()))

    @Test fun credentialsRequireValidEmailAndPassword() {
        val empty = assertIs<ValidationResult.Invalid>(CredentialsValidator.validate("", "", isSignUp = false))
        assertEquals(setOf(CredentialsValidator.FIELD_EMAIL, CredentialsValidator.FIELD_PASSWORD), empty.errors.keys)
        assertIs<ValidationResult.Invalid>(CredentialsValidator.validate("alumno@", "12345678", isSignUp = false))
        assertEquals(ValidationResult.Valid, CredentialsValidator.validate("ana@alumno.ipn.mx", "123", isSignUp = false))
    }

    @Test fun signUpRequiresEightCharacters() {
        assertIs<ValidationResult.Invalid>(CredentialsValidator.validate("ana@alumno.ipn.mx", "1234567", isSignUp = true))
        assertEquals(ValidationResult.Valid, CredentialsValidator.validate(" ana@alumno.ipn.mx ", "12345678", isSignUp = true))
    }

    @Test fun phoneValidation() {
        assertTrue(CredentialsValidator.isValidPhone("55 1234 5678"))
        assertFalse(CredentialsValidator.isValidPhone("123"))
    }
}
