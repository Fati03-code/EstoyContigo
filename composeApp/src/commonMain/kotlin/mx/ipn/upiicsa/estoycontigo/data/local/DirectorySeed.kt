package mx.ipn.upiicsa.estoycontigo.data.local

import mx.ipn.upiicsa.estoycontigo.data.local.entity.SupportServiceEntity
import mx.ipn.upiicsa.estoycontigo.domain.model.ServiceCategory

/**
 * Directorio precargado en Room para que funcione sin internet (alguien en crisis puede no tener datos).
 * Datos verificados en fuentes oficiales en noviembre de 2026. Revisar cada semestre y subir SEED_VERSION.
 */
object DirectorySeed {
    const val SEED_VERSION = 1
    const val VERIFIED_LABEL = "Información verificada en noviembre de 2026"

    const val LINEA_DE_LA_VIDA = "8009112000"
    const val EMERGENCIAS = "911"

    val services: List<SupportServiceEntity> = listOf(
        SupportServiceEntity(
            id = "linea_vida",
            name = "Línea de la Vida (CONASAMA)",
            description = "Orientación gratuita y confidencial en salud mental y adicciones: crisis emocionales, prevención del suicidio, ansiedad, depresión y estrés.",
            category = ServiceCategory.CRISIS.name,
            phone = LINEA_DE_LA_VIDA, phoneExtension = null, whatsapp = null, email = null,
            address = null,
            schedule = "24 horas, los 365 días",
            is24h = true, latitude = null, longitude = null,
            website = "https://www.gob.mx/conasama/es/articulos/linea-de-la-vida-800-911-2000",
            sortOrder = 0,
        ),
        SupportServiceEntity(
            id = "emergencias_911",
            name = "Emergencias 911",
            description = "Si tu vida o la de alguien más está en peligro inmediato.",
            category = ServiceCategory.CRISIS.name,
            phone = EMERGENCIAS, phoneExtension = null, whatsapp = null, email = null,
            address = null,
            schedule = "24 horas",
            is24h = true, latitude = null, longitude = null, website = null,
            sortOrder = 1,
        ),
        SupportServiceEntity(
            id = "orientacion_upiicsa",
            name = "Orientación Juvenil UPIICSA",
            description = "Atención psicológica de primer contacto y canalización, consejería educativa y talleres sobre estrés, ansiedad, autoestima y emociones.",
            category = ServiceCategory.UPIICSA.name,
            phone = "5556242000", phoneExtension = "70129", whatsapp = null,
            email = "orientacionupiicsa@ipn.mx",
            address = "Edificio de Gobierno, 1er piso. Av. Té 950, Granjas México, Iztacalco, CDMX",
            schedule = "Lunes a viernes 8:00–14:00 y 16:00–20:00",
            is24h = false, latitude = 19.39393, longitude = -99.09038,
            website = "https://www.upiicsa.ipn.mx/estudiantes/orientacion-juvenil.html",
            sortOrder = 2,
        ),
        SupportServiceEntity(
            id = "salud_upiicsa",
            name = "Atención a la Salud UPIICSA",
            description = "Servicio médico de la unidad. Puede orientarte y canalizarte si tu malestar también es físico.",
            category = ServiceCategory.UPIICSA.name,
            phone = "5556242000", phoneExtension = "70015", whatsapp = null,
            email = "atesalud_upiicsa@ipn.mx",
            address = "Edificio de Gobierno, planta baja. Av. Té 950, Iztacalco, CDMX",
            schedule = "Horario escolar",
            is24h = false, latitude = 19.39393, longitude = -99.09038, website = null,
            sortOrder = 3,
        ),
        SupportServiceEntity(
            id = "saptel",
            name = "SAPTEL",
            description = "Sistema Nacional de Apoyo, Consejo Psicológico e Intervención en Crisis por Teléfono. Gratuito.",
            category = ServiceCategory.CRISIS.name,
            phone = "5552598121", phoneExtension = null, whatsapp = null, email = null,
            address = null,
            schedule = "24 horas",
            is24h = true, latitude = null, longitude = null, website = "https://www.saptel.org.mx",
            sortOrder = 4,
        ),
        SupportServiceEntity(
            id = "consejo_ciudadano",
            name = "Consejo Ciudadano CDMX",
            description = "Apoyo psicológico gratuito por teléfono o Chat de Confianza de WhatsApp. También orientación jurídica.",
            category = ServiceCategory.CRISIS.name,
            phone = "5555335533", phoneExtension = null, whatsapp = "525555335533", email = null,
            address = null,
            schedule = "24 horas, los 365 días",
            is24h = true, latitude = null, longitude = null, website = "https://consejociudadanomx.org",
            sortOrder = 5,
        ),
        SupportServiceEntity(
            id = "locatel",
            name = "Locatel – Servicio psicológico",
            description = "Atención psicológica telefónica y por chat del Gobierno de la CDMX. También puedes marcar *0311.",
            category = ServiceCategory.CRISIS.name,
            phone = "5556581111", phoneExtension = null, whatsapp = null, email = null,
            address = null,
            schedule = "24 horas, los 365 días",
            is24h = true, latitude = null, longitude = null, website = "https://311locatel.cdmx.gob.mx/psicologia.xhtml",
            sortOrder = 6,
        ),
        SupportServiceEntity(
            id = "fray_bernardino",
            name = "Hospital Psiquiátrico Fray Bernardino Álvarez",
            description = "Urgencias psiquiátricas abiertas todo el día. Acude si hay riesgo inmediato para ti o para otra persona.",
            category = ServiceCategory.CLINIC.name,
            phone = "5556554405", phoneExtension = "23979", whatsapp = null, email = null,
            address = "Av. San Buenaventura 2, Niño Jesús, Tlalpan, CDMX",
            schedule = "Urgencias 24 horas",
            is24h = true, latitude = 19.28785, longitude = -99.16063,
            website = "https://www.gob.mx/conasama/articulos/hospital-psiquiatrico-fray-bernardino-alvarez-362887",
            sortOrder = 7,
        ),
        SupportServiceEntity(
            id = "inprf",
            name = "Instituto Nacional de Psiquiatría Ramón de la Fuente Muñiz",
            description = "Atención especializada en salud mental (consulta con cita y referencia).",
            category = ServiceCategory.CLINIC.name,
            phone = "5541605000", phoneExtension = null, whatsapp = null, email = null,
            address = "Calz. México-Xochimilco 101, San Lorenzo Huipulco, Tlalpan, CDMX",
            schedule = "Consulta con cita",
            is24h = false, latitude = 19.29276, longitude = -99.15248, website = "https://inprf.gob.mx",
            sortOrder = 8,
        ),
        SupportServiceEntity(
            id = "unam_davila",
            name = "Centro de Servicios Psicológicos «Dr. Guillermo Dávila» (UNAM)",
            description = "Terapia individual, grupal, de pareja o familia y talleres, con cuota accesible.",
            category = ServiceCategory.CLINIC.name,
            phone = "5556222309", phoneExtension = null, whatsapp = null, email = null,
            address = "Av. Universidad 3004, Edif. D, sótano, Facultad de Psicología, Coyoacán, CDMX",
            schedule = "Lunes a viernes 8:30–19:00",
            is24h = false, latitude = 19.33568, longitude = -99.18856, website = "https://www.psicologia.unam.mx/centros-de-formacion/",
            sortOrder = 9,
        ),
    )
}
