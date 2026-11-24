package mx.ipn.upiicsa.estoycontigo.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import mx.ipn.upiicsa.estoycontigo.MainActivity
import mx.ipn.upiicsa.estoycontigo.R

object Notifications {
    const val CHANNEL_REMINDERS = "recordatorios"
    const val CHANNEL_EXAMS = "examenes"
    const val EXTRA_DESTINATION = "destination"
    private const val ID_DAILY = 1001
    private const val ID_EXAM = 1002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Recordatorio diario", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Un recordatorio amable para registrar cómo te sientes." },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_EXAMS, "Época de exámenes", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Pausas breves sugeridas durante periodos de evaluación." },
        )
    }

    fun showDailyReminder(context: Context) = show(
        context, ID_DAILY, CHANNEL_REMINDERS,
        title = "¿Cómo te sientes hoy?",
        text = "Michi está aquí. Registrar tu ánimo toma menos de un minuto.",
        destination = "mood",
    )

    fun showExamTip(context: Context, text: String) = show(
        context, ID_EXAM, CHANNEL_EXAMS,
        title = "Pausa de 1 minuto",
        text = text,
        destination = "breathing",
    )

    private fun show(context: Context, id: Int, channel: String, title: String, text: String, destination: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 33
        ) return
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_DESTINATION, destination)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            // No se muestra contenido emocional en la pantalla de bloqueo.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
        }
    }
}
