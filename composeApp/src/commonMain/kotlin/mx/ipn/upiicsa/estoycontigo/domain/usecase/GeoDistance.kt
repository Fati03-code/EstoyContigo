package mx.ipn.upiicsa.estoycontigo.domain.usecase

import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoDistance {
    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(a: GeoPoint, b: GeoPoint): Double {
        val dLat = (b.latitude - a.latitude).toRadians()
        val dLon = (b.longitude - a.longitude).toRadians()
        val h = sin(dLat / 2).pow(2) + cos(a.latitude.toRadians()) * cos(b.latitude.toRadians()) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
    }

    fun format(km: Double): String = if (km < 1) "${(km * 1000).toInt()} m" else "${(km * 10).toInt() / 10.0} km"

    private fun Double.toRadians() = this * kotlin.math.PI / 180.0
}
