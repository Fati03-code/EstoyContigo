package mx.ipn.upiicsa.estoycontigo

import android.app.Application
import androidx.work.Configuration
import mx.ipn.upiicsa.estoycontigo.di.initKoin
import mx.ipn.upiicsa.estoycontigo.platform.SyncScheduler
import mx.ipn.upiicsa.estoycontigo.work.Notifications
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory

class EstoyContigoApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        initKoin(androidModule(this)) {
            androidContext(this@EstoyContigoApp)
            workManagerFactory()
        }
        Notifications.createChannels(this)
        get<SyncScheduler>().apply {
            schedulePeriodic()
            requestSync()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(KoinWorkerFactory()).build()
}
