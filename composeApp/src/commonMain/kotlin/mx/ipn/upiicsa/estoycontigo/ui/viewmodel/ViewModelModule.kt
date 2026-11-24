package mx.ipn.upiicsa.estoycontigo.ui.viewmodel

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MoodViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::BreathingViewModel)
    viewModelOf(::DirectoryViewModel)
    viewModelOf(::SettingsViewModel)
}
