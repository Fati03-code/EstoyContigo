package mx.ipn.upiicsa.estoycontigo.ui.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import mx.ipn.upiicsa.estoycontigo.platform.nowMillis

object Dates {
    private val tz get() = TimeZone.currentSystemDefault()
    private val days = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
    private val months = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    fun localDate(millis: Long): LocalDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz).date
    fun today(): LocalDate = localDate(nowMillis())

    fun dayLabel(date: LocalDate): String {
        val today = today()
        return when (date) {
            today -> "Hoy"
            today.minus(DatePeriod(days = 1)) -> "Ayer"
            else -> "${days[date.dayOfWeek.ordinal]} ${date.day} ${months[date.month.ordinal]}"
        }
    }

    fun shortDay(date: LocalDate): String = days[date.dayOfWeek.ordinal].replaceFirstChar { it.uppercase() }

    fun time(millis: Long): String {
        val t = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
        return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
    }

    fun hhmm(hour: Int, minute: Int) = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun greeting(): String {
        val hour = Instant.fromEpochMilliseconds(nowMillis()).toLocalDateTime(tz).hour
        return when (hour) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    /** Últimos [n] días, del más antiguo a hoy. */
    fun lastDays(n: Int): List<LocalDate> = (n - 1 downTo 0).map { today().minus(DatePeriod(days = it)) }
}
