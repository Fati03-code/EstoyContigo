package mx.ipn.upiicsa.estoycontigo.domain

import mx.ipn.upiicsa.estoycontigo.data.local.DirectorySeed
import mx.ipn.upiicsa.estoycontigo.data.local.toDomain
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.GeoPoint
import mx.ipn.upiicsa.estoycontigo.domain.model.InterventionKind
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory
import mx.ipn.upiicsa.estoycontigo.domain.usecase.GeoDistance
import mx.ipn.upiicsa.estoycontigo.domain.usecase.InterventionCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalogAndGeoTest {
    @Test fun allInterventionsAreMicroInterventionsOfOneToThreeMinutes() {
        InterventionCatalog.all.forEach {
            assertTrue(it.durationSeconds in 60..180, "${it.id} dura ${it.durationSeconds}s")
            when (val k = it.kind) {
                is InterventionKind.Breathing -> assertTrue(k.phases.isNotEmpty())
                is InterventionKind.Guided -> assertTrue(k.steps.isNotEmpty())
            }
        }
        assertEquals(InterventionCatalog.all.size, InterventionCatalog.all.map { it.id }.toSet().size)
    }

    @Test fun everyEmotionHasARecommendation() {
        (Emotion.entries + listOf(null)).forEach { e ->
            assertNotNull(InterventionCatalog.byId(InterventionCatalog.recommendedFor(e).id))
        }
    }

    @Test fun haversineUpiicsaToZocaloIsAboutSevenKm() {
        val upiicsa = GeoPoint(19.39393, -99.09038)
        val zocalo = GeoPoint(19.43260, -99.13320)
        val km = GeoDistance.haversineKm(upiicsa, zocalo)
        assertTrue(km in 6.0..7.5, "distancia $km")
        assertEquals(0.0, GeoDistance.haversineKm(upiicsa, upiicsa), 1e-9)
        assertEquals("850 m", GeoDistance.format(0.85))
        assertEquals("6.2 km", GeoDistance.format(6.27))
    }

    @Test fun offlineDirectoryContainsCrisisLinesAndUpiicsa() {
        val services = DirectorySeed.services.map { it.toDomain() }
        val vida = services.first { it.id == "linea_vida" }
        assertEquals("8009112000", vida.phone)
        assertTrue(vida.is24h)
        assertTrue(services.any { it.id == "emergencias_911" && it.phone == "911" })
        assertTrue(services.any { it.category == ServiceCategory.UPIICSA })
        assertTrue(services.filter { it.category == ServiceCategory.CRISIS }.all { it.phone != null })
        assertEquals(services.size, services.map { it.id }.toSet().size)
    }
}
