package mx.ipn.upiicsa.estoycontigo

import androidx.room.Room
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import mx.ipn.upiicsa.estoycontigo.data.local.AppDatabase
import mx.ipn.upiicsa.estoycontigo.data.local.buildDatabase
import mx.ipn.upiicsa.estoycontigo.data.prefs.DATA_STORE_FILE
import mx.ipn.upiicsa.estoycontigo.data.prefs.createDataStore
import mx.ipn.upiicsa.estoycontigo.platform.ConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.ExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.Haptics
import mx.ipn.upiicsa.estoycontigo.platform.IosConnectivityMonitor
import mx.ipn.upiicsa.estoycontigo.platform.IosExternalActions
import mx.ipn.upiicsa.estoycontigo.platform.IosHaptics
import mx.ipn.upiicsa.estoycontigo.platform.IosLocationProvider
import mx.ipn.upiicsa.estoycontigo.platform.IosMotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.IosPhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.IosReminderScheduler
import mx.ipn.upiicsa.estoycontigo.platform.IosSyncScheduler
import mx.ipn.upiicsa.estoycontigo.platform.LocationProvider
import mx.ipn.upiicsa.estoycontigo.platform.MotionSensor
import mx.ipn.upiicsa.estoycontigo.platform.PhotoStorage
import mx.ipn.upiicsa.estoycontigo.platform.ReminderScheduler
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.platform.appSupportDir
import org.koin.dsl.module

val iosModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<AppDatabase> { buildDatabase(Room.databaseBuilder<AppDatabase>(name = "${appSupportDir()}/${AppDatabase.FILE_NAME}")) }
    single { createDataStore { "${appSupportDir()}/$DATA_STORE_FILE" } }
    single<PhotoStorage> { IosPhotoStorage() }
    single<MotionSensor> { IosMotionSensor() }
    single<LocationProvider> { IosLocationProvider() }
    single<ReminderScheduler> { IosReminderScheduler() }
    single<SyncScheduler> { IosSyncScheduler() }
    single<ConnectivityMonitor> { IosConnectivityMonitor() }
    single<ExternalActions> { IosExternalActions() }
    single<Haptics> { IosHaptics() }
}
