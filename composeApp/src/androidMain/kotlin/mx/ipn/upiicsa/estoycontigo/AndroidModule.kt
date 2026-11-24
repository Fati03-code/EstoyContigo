package mx.ipn.upiicsa.estoycontigo

import android.content.Context
import androidx.room.Room
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import mx.ipn.upiicsa.estoycontigo.data.local.AppDatabase
import mx.ipn.upiicsa.estoycontigo.data.local.buildDatabase
import mx.ipn.upiicsa.estoycontigo.data.prefs.DATA_STORE_FILE
import mx.ipn.upiicsa.estoycontigo.data.prefs.createDataStore
import mx.ipn.upiicsa.estoycontigo.platform.AndroidConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.AndroidExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.AndroidHaptics
import mx.ipn.upiicsa.estoycontigo.platform.AndroidLocationProvider
import mx.ipn.upiicsa.estoycontigo.platform.AndroidMotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.AndroidPhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.ConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.Haptics
import mx.ipn.upiicsa.estoycontigo.platform.LocationProvider
import mx.ipn.upiicsa.estoycontigo.platform.MotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.PhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.ReminderScheduler
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.work.AndroidReminderScheduler
import mx.ipn.upiicsa.estoycontigo.work.AndroidSyncScheduler
import mx.ipn.upiicsa.estoycontigo.work.DailyReminderWorker
import mx.ipn.upiicsa.estoycontigo.work.ExamModeWorker
import mx.ipn.upiicsa.estoycontigo.work.SyncWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<AppDatabase> {
        val file = context.getDatabasePath(AppDatabase.FILE_NAME)
        buildDatabase(Room.databaseBuilder<AppDatabase>(context, file.absolutePath))
    }
    single { createDataStore { context.filesDir.resolve(DATA_STORE_FILE).absolutePath } }
    single<PhotoStorage> { AndroidPhotoStorage(context) }
    single<MotionSensor> { AndroidMotionSensor(context) }
    single<LocationProvider> { AndroidLocationProvider(context) }
    single<ReminderScheduler> { AndroidReminderScheduler(context) }
    single<SyncScheduler> { AndroidSyncScheduler(context) }
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(context) }
    single<ExternalActions> { AndroidExternalActions(context) }
    single<Haptics> { AndroidHaptics(context) }
    workerOf(::SyncWorker)
    workerOf(::DailyReminderWorker)
    workerOf(::ExamModeWorker)
}
