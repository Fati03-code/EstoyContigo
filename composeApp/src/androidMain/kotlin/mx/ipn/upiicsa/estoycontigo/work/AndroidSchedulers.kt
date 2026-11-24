package mx.ipn.upiicsa.estoycontigo.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import mx.ipn.upiicsa.estoycontigo.platform.ReminderScheduler
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class AndroidSyncScheduler(context: Context) : SyncScheduler {
    private val wm = WorkManager.getInstance(context)
    private val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        // APPEND_OR_REPLACE garantiza que un registro guardado durante una sincronización también se suba.
        wm.enqueueUniqueWork(SYNC_NOW, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    override fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(networkConstraint)
            .build()
        wm.enqueueUniquePeriodicWork(SYNC_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun cancelAll() {
        wm.cancelUniqueWork(SYNC_NOW)
        wm.cancelUniqueWork(SYNC_PERIODIC)
    }

    companion object {
        const val SYNC_NOW = "sync_now"
        const val SYNC_PERIODIC = "sync_periodic"
    }
}

class AndroidReminderScheduler(context: Context) : ReminderScheduler {
    private val wm = WorkManager.getInstance(context)

    override fun scheduleDaily(hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    override fun cancelDaily() {
        wm.cancelUniqueWork(DAILY)
    }

    override fun setExamMode(enabled: Boolean) {
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<ExamModeWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayUntil(12, 30), TimeUnit.MILLISECONDS)
                .build()
            wm.enqueueUniquePeriodicWork(EXAM, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            wm.cancelUniqueWork(EXAM)
        }
    }

    private fun delayUntil(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var next = now.with(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    companion object {
        const val DAILY = "daily_reminder"
        const val EXAM = "exam_mode"
    }
}
