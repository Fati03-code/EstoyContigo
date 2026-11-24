package mx.ipn.upiicsa.estoycontigo.data.remote

class RemoteException(val code: Int, override val message: String) : Exception(message)

fun authErrorToSpanish(raw: String): String = when {
    raw.contains("EMAIL_EXISTS") -> "Ese correo ya tiene una cuenta. Intenta iniciar sesión."
    raw.contains("INVALID_LOGIN_CREDENTIALS") || raw.contains("INVALID_PASSWORD") || raw.contains("EMAIL_NOT_FOUND") ->
        "Correo o contraseña incorrectos."
    raw.contains("WEAK_PASSWORD") -> "La contraseña es muy débil: usa al menos 8 caracteres."
    raw.contains("INVALID_EMAIL") -> "El correo no es válido."
    raw.contains("TOO_MANY_ATTEMPTS") -> "Demasiados intentos. Espera unos minutos."
    raw.contains("USER_DISABLED") -> "Esta cuenta está deshabilitada."
    else -> "No pudimos conectar con el servidor. Revisa tu conexión e inténtalo de nuevo."
}
