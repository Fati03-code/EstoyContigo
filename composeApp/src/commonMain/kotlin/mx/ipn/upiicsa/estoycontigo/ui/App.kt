package mx.ipn.upiicsa.estoycontigo.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.ipn.upiicsa.estoycontigo.domain.model.ThemeMode
import mx.ipn.upiicsa.estoycontigo.ui.components.Michi
import mx.ipn.upiicsa.estoycontigo.ui.components.MichiMood
import mx.ipn.upiicsa.estoycontigo.ui.navigation.Route
import mx.ipn.upiicsa.estoycontigo.ui.screens.AboutScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.AccountScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.BreathingScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.CrisisScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.DirectoryScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.HistoryScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.HomeScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.InterventionsScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.MoodScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.OnboardingScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.PrivacyScreen
import mx.ipn.upiicsa.estoycontigo.ui.screens.SettingsScreen
import mx.ipn.upiicsa.estoycontigo.ui.theme.EstoyContigoTheme
import mx.ipn.upiicsa.estoycontigo.ui.viewmodel.AppViewModel
import org.koin.compose.viewmodel.koinViewModel

private data class TopLevel(val route: Route, val label: String, val icon: ImageVector)

private val topLevel = listOf(
    TopLevel(Route.Home, "Inicio", Icons.Outlined.Home),
    TopLevel(Route.Interventions, "Pausas", Icons.Outlined.SelfImprovement),
    TopLevel(Route.History, "Diario", Icons.AutoMirrored.Outlined.MenuBook),
    TopLevel(Route.Directory, "Apoyo", Icons.Outlined.LocationOn),
)

@Composable
fun App(deepLink: StateFlow<String?>, onDeepLinkHandled: () -> Unit) {
    val appVm = koinViewModel<AppViewModel>()
    val state by appVm.state.collectAsStateWithLifecycle()
    val settings = state.settings

    EstoyContigoTheme(settings?.themeMode ?: ThemeMode.SYSTEM) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (settings == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Michi(size = 120.dp, mood = MichiMood.CALM)
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
                return@Surface
            }
            val start: Route = when {
                !settings.onboardingDone -> Route.Onboarding
                !settings.accountStepDone -> Route.Account
                else -> Route.Home
            }
            val nav = rememberNavController()
            val link by deepLink.collectAsStateWithLifecycle()
            LaunchedEffect(link, settings.onboardingDone) {
                val target = when (link) {
                    "mood" -> Route.Mood
                    "breathing" -> Route.Interventions
                    else -> null
                }
                if (target != null && settings.onboardingDone && settings.accountStepDone) {
                    nav.navigate(target) { launchSingleTop = true }
                    onDeepLinkHandled()
                }
            }
            val consentGiven = rememberUpdatedState(settings.onboardingDone && settings.accountStepDone)
            LaunchedEffect(Unit) {
                appVm.shakes.collect {
                    val current = nav.currentBackStackEntry?.destination
                    val inMainFlow = topLevel.any { t -> current?.hasRoute(t.route::class) == true }
                    if (inMainFlow && consentGiven.value) nav.navigate(Route.Mood) { launchSingleTop = true }
                }
            }
            MainShell(nav, start, state.isOnline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(nav: NavHostController, start: Route, isOnline: Boolean) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val mainUnlocked = start == Route.Home
    val isTopLevel = mainUnlocked && topLevel.any { destination?.hasRoute(it.route::class) == true }

    fun go(route: Route) {
        scope.launch { drawerState.close() }
        nav.navigate(route) { launchSingleTop = true }
    }

    fun goTop(route: Route) {
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevel,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Michi(size = 88.dp, mood = MichiMood.HAPPY)
                        Text("EstoyContigo", style = MaterialTheme.typography.titleLarge)
                        Text("Acompañamiento emocional · UPIICSA", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DrawerItem("Inicio", Icons.Outlined.Home) { scope.launch { drawerState.close() }; goTop(Route.Home) }
                    DrawerItem("Registrar mi ánimo", Icons.Outlined.Favorite) { go(Route.Mood) }
                    DrawerItem("Micro-pausas", Icons.Outlined.SelfImprovement) { scope.launch { drawerState.close() }; goTop(Route.Interventions) }
                    DrawerItem("Mi diario", Icons.AutoMirrored.Outlined.MenuBook) { scope.launch { drawerState.close() }; goTop(Route.History) }
                    DrawerItem("Directorio de apoyo", Icons.Outlined.LocationOn) { scope.launch { drawerState.close() }; goTop(Route.Directory) }
                    DrawerItem("Ayuda inmediata (SOS)", Icons.Outlined.SupportAgent) { go(Route.Crisis) }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DrawerItem("Mi cuenta y respaldo", Icons.Outlined.Person) { go(Route.Account) }
                    DrawerItem("Ajustes", Icons.Outlined.Settings) { go(Route.Settings) }
                    DrawerItem("Privacidad", Icons.Outlined.PrivacyTip) { go(Route.Privacy) }
                    DrawerItem("Acerca de y aviso", Icons.Outlined.Info) { go(Route.About) }
                }
            }
        },
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevel) {
                    Column {
                        if (!isOnline) OfflineBanner()
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                            topLevel.forEach { item ->
                                NavigationBarItem(
                                    selected = destination?.hasRoute(item.route::class) == true,
                                    onClick = { goTop(item.route) },
                                    icon = { Icon(item.icon, null) },
                                    label = { Text(item.label) },
                                )
                            }
                        }
                    }
                }
            },
        ) { padding ->
            val openDrawer: () -> Unit = {
                if (mainUnlocked) scope.launch { drawerState.open() } else nav.popBackStack()
            }
            NavHost(
                navController = nav,
                startDestination = start,
                modifier = Modifier.padding(PaddingValues(bottom = padding.calculateBottomPadding())),
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
            ) {
                composable<Route.Onboarding> {
                    OnboardingScreen(onFinish = {
                        nav.navigate(Route.Account) { popUpTo(Route.Onboarding) { inclusive = true } }
                    }, onCrisis = { nav.navigate(Route.Crisis) })
                }
                composable<Route.Account> {
                    AccountScreen(
                        canGoBack = nav.previousBackStackEntry != null,
                        onBack = { nav.popBackStack() },
                        onDone = {
                            if (nav.previousBackStackEntry != null) nav.popBackStack()
                            else nav.navigate(Route.Home) { popUpTo(Route.Account) { inclusive = true } }
                        },
                    )
                }
                composable<Route.Home> {
                    HomeScreen(
                        onMenu = openDrawer,
                        onSos = { nav.navigate(Route.Crisis) },
                        onRegister = { nav.navigate(Route.Mood) },
                        onOpenIntervention = { nav.navigate(Route.Breathing(it)) },
                        onInterventions = { goTop(Route.Interventions) },
                        onHistory = { goTop(Route.History) },
                        onDirectory = { goTop(Route.Directory) },
                    )
                }
                composable<Route.Mood> {
                    MoodScreen(
                        onClose = { nav.popBackStack() },
                        onOpenIntervention = { id ->
                            nav.navigate(Route.Breathing(id)) { popUpTo(Route.Mood) { inclusive = true } }
                        },
                        onCrisis = { nav.navigate(Route.Crisis) { popUpTo(Route.Mood) { inclusive = true } } },
                        onDirectory = { nav.navigate(Route.Directory) { popUpTo(Route.Mood) { inclusive = true } } },
                    )
                }
                composable<Route.History> {
                    HistoryScreen(onMenu = openDrawer, onSos = { nav.navigate(Route.Crisis) },
                        onRegister = { nav.navigate(Route.Mood) }, onAccount = { nav.navigate(Route.Account) })
                }
                composable<Route.Interventions> {
                    InterventionsScreen(onMenu = openDrawer, onSos = { nav.navigate(Route.Crisis) },
                        onOpen = { nav.navigate(Route.Breathing(it)) })
                }
                composable<Route.Breathing> { entry ->
                    BreathingScreen(id = entry.toRoute<Route.Breathing>().id, onBack = { nav.popBackStack() },
                        onSos = { nav.navigate(Route.Crisis) })
                }
                composable<Route.Directory> {
                    DirectoryScreen(onMenu = openDrawer, onSos = { nav.navigate(Route.Crisis) })
                }
                composable<Route.Crisis> {
                    CrisisScreen(onBack = { nav.popBackStack() }, onDirectory = {
                            if (mainUnlocked) goTop(Route.Directory) else nav.navigate(Route.Directory)
                        },
                        onBreathing = { nav.navigate(Route.Breathing("suspiro")) })
                }
                composable<Route.Settings> {
                    SettingsScreen(
                        onBack = { nav.popBackStack() },
                        onPrivacy = { nav.navigate(Route.Privacy) },
                        onAccount = { nav.navigate(Route.Account) },
                        onDataDeleted = {
                            nav.navigate(Route.Onboarding) { popUpTo(0) { inclusive = true } }
                        },
                    )
                }
                composable<Route.Privacy> { PrivacyScreen(onBack = { nav.popBackStack() }) }
                composable<Route.About> { AboutScreen(onBack = { nav.popBackStack() }, onCrisis = { nav.navigate(Route.Crisis) }) }
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, null) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

@Composable
private fun OfflineBanner() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.WifiOff, null, modifier = Modifier.padding(end = 8.dp))
            Text("Sin conexión: todo se guarda en tu teléfono y se respaldará al reconectar.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
