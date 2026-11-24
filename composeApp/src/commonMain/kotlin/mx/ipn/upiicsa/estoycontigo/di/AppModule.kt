package mx.ipn.upiicsa.estoycontigo.di

import mx.ipn.upiicsa.estoycontigo.config.FirebaseConfig
import mx.ipn.upiicsa.estoycontigo.data.local.AppDatabase
import mx.ipn.upiicsa.estoycontigo.data.prefs.PreferencesStore
import mx.ipn.upiicsa.estoycontigo.data.remote.FirebaseAuthApi
import mx.ipn.upiicsa.estoycontigo.data.remote.FirestoreApi
import mx.ipn.upiicsa.estoycontigo.data.remote.StorageApi
import mx.ipn.upiicsa.estoycontigo.data.remote.createHttpClient
import mx.ipn.upiicsa.estoycontigo.data.repository.AuthRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.DirectoryRepository
import mx.ipn.upiicsa.estoycontigo.data.repository.MoodRepository
import mx.ipn.upiicsa.estoycontigo.data.sync.SyncManager
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.viewModelModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule = module {
    single { FirebaseConfig.fromBuild() }
    single { createHttpClient(get()) }
    single { PreferencesStore(get()) }
    single { get<AppDatabase>().moodDao() }
    single { get<AppDatabase>().directoryDao() }
    single { FirebaseAuthApi(get(), get()) }
    single { FirestoreApi(get(), get()) }
    single { StorageApi(get(), get()) }
    single { AuthRepository(get(), get(), get()) }
    single { MoodRepository(get(), get(), get()) }
    single { DirectoryRepository(get(), get()) }
    single { SyncManager(get(), get(), get(), get(), get(), get()) }
}

/** `platformModule` provee: HttpClientEngine, AppDatabase, DataStore y los servicios de hardware. */
fun initKoin(platformModule: Module, extra: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    extra()
    modules(platformModule, dataModule, viewModelModule)
}
