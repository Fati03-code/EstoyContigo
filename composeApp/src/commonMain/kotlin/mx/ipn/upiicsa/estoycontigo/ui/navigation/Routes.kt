package mx.ipn.upiicsa.estoycontigo.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Account : Route
    @Serializable data object Home : Route
    @Serializable data object Mood : Route
    @Serializable data object History : Route
    @Serializable data object Interventions : Route
    @Serializable data class Breathing(val id: String) : Route
    @Serializable data object Directory : Route
    @Serializable data object Crisis : Route
    @Serializable data object Settings : Route
    @Serializable data object Privacy : Route
    @Serializable data object About : Route
}
