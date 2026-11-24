package mx.ipn.upiicsa.estoycontigo.domain.model

enum class ServiceCategory(val label: String) {
    CRISIS("Líneas 24 h"),
    UPIICSA("UPIICSA / IPN"),
    CLINIC("Atención especializada"),
}

data class SupportService(
    val id: String,
    val name: String,
    val description: String,
    val category: ServiceCategory,
    val phone: String?,
    val phoneExtension: String?,
    val whatsapp: String?,
    val email: String?,
    val address: String?,
    val schedule: String,
    val is24h: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val website: String?,
    val distanceKm: Double? = null,
)

data class GeoPoint(val latitude: Double, val longitude: Double)
