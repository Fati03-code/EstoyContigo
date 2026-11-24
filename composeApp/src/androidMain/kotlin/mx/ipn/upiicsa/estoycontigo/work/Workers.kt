package mx.ipn.upiicsa.estoycontigo.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncManager
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncResult

/** Sube los registros pendientes (naranja) cuando hay red; WorkManager reintenta con backoff exponencial. */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncManager: SyncManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = when (syncManager.syncPending()) {
        is SyncResult.Success, is SyncResult.Skipped -> Result.success()
        is SyncResult.Partial -> if (runAttemptCount < 5) Result.retry() else Result.failure()
    }
}

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val prefs: PreferencesStore,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (prefs.currentSettings().reminderEnabled) Notifications.showDailyReminder(applicationContext)
        return Result.success()
    }
}

class ExamModeWorker(
    context: Context,
    params: WorkerParameters,
    private val prefs: PreferencesStore,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (prefs.currentSettings().examMode) {
            Notifications.showExamTip(applicationContext, EXAM_TIPS.random())
        }
        return Result.success()
    }

    companion object {
        val EXAM_TIPS = listOf(
            "Antes de seguir estudiando, prueba un suspiro fisiológico: 60 segundos.",
            "Tu valor no depende de una calificación. ¿Una respiración cuadrada de 2 minutos?",
            "Entre clases: suelta hombros y mandíbula. Michi te guía en 90 segundos.",
            "¿Comiste y tomaste agua hoy? Cuidarte también es estudiar.",
            "Si el estrés se vuelve demasiado, Orientación Juvenil de UPIICSA puede escucharte.",
        )
    }
}
